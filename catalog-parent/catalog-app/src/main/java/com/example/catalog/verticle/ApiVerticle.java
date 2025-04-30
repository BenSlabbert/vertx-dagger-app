/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.catalog.service.Consumer;
import com.example.catalog.web.route.handler.AuthHandler;
import com.example.catalog.web.route.handler.ItemHandler;
import github.benslabbert.vertxdaggercommons.closer.ClosingService;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.future.FutureUtil;
import github.benslabbert.vertxdaggercommons.future.MultiCompletePromise;
import github.benslabbert.vertxdaggercommons.security.SecurityHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Pool;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(ApiVerticle.class);

  private final ClosingService closingService;
  private final Set<Consumer> consumers;
  private final ItemHandler itemHandler;
  private final AuthHandler authHandler;
  private final RedisAPI redisAPI;
  private final Config config;
  private final Pool pool;

  private HttpServer httpServer;

  @Inject
  ApiVerticle(
      ClosingService closingService,
      Set<Consumer> consumers,
      ItemHandler itemHandler,
      AuthHandler authHandler,
      RedisAPI redisAPI,
      Config config,
      Pool pool) {
    this.closingService = closingService;
    this.consumers = consumers;
    this.itemHandler = itemHandler;
    this.authHandler = authHandler;
    this.redisAPI = redisAPI;
    this.config = config;
    this.pool = pool;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.exceptionHandler(err -> log.error("unhandled exception", err));
    log.info("starting ApiVerticle");

    checkConnections(startPromise)
        .onSuccess(
            ignore -> {
              Config.HttpConfig httpConfig = config.httpConfig();
              log.info("starting api verticle on port: {}", httpConfig.port());

              vertx
                  .createHttpServer(
                      new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
                  .requestHandler(setupRoutes())
                  .listen()
                  .onComplete(
                      res -> {
                        if (res.succeeded()) {
                          log.info("started http server");
                          log.info("register consumers");
                          consumers.forEach(Consumer::register);
                          httpServer = res.result();
                          startPromise.complete();
                        } else {
                          log.error("failed to start verticle", res.cause());
                          startPromise.fail(res.cause());
                        }
                      });
            });
  }

  private Router setupRoutes() {
    Router mainRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    mainRouter
        .route()
        // CORS config
        .handler(CorsHandler.create())
        // auth handler
        .handler(authHandler);

    // main routes
    mainRouter
        .route("/api/*")
        // 100kB max body size
        .handler(BodyHandler.create().setBodyLimit(1024L * 100L))
        .subRouter(apiRouter);

    // roles added in dagger.authHandler()
    // ensure request is authenticated correctly
    apiRouter.route().handler(ctx -> SecurityHandler.hasRole(ctx, AuthHandler.ROLE));

    // api routes
    itemHandler.configureRoutes(apiRouter);

    mainRouter
        .get("/health*")
        .handler(
            ctx ->
                getHealthCheckHandler()
                    .checkStatus()
                    .onComplete(r -> ctx.response().write(r.result().getData().toBuffer())));

    // all unmatched requests go here
    mainRouter.route("/*").handler(ctx -> ctx.response().setStatusCode(NOT_FOUND.code()).end());
    return mainRouter;
  }

  private HealthChecks getHealthCheckHandler() {
    return HealthChecks.create(vertx)
        .register("available", promise -> promise.complete(Status.OK()));
  }

  private Future<?> checkConnections(Promise<Void> startPromise) {
    return checkDbConnection(startPromise);
  }

  private Future<?> checkDbConnection(Promise<Void> startPromise) {
    return pool.getConnection()
        .onFailure(startPromise::fail)
        .onSuccess(
            conn ->
                conn.close()
                    .onFailure(startPromise::fail)
                    .map(ignore -> checkRedisConnection(startPromise)));
  }

  private Future<?> checkRedisConnection(Promise<Void> startPromise) {
    return redisAPI.ping(List.of()).onFailure(startPromise::fail);
  }

  public int getPort() {
    return httpServer.actualPort();
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void stop(Promise<Void> stopPromise) {
    System.err.println("stopping");
    MultiCompletePromise multiCompletePromise = MultiCompletePromise.create(stopPromise, 3);

    httpServer.close().onComplete(multiCompletePromise::complete);

    Set<AutoCloseable> closeables = closingService.closeables();
    System.err.printf("closing created resources [%d]...%n", closeables.size());

    Future.all(consumers.stream().map(Consumer::unregister).toList())
        .onComplete(
            ignore -> {
              System.err.println("all eventbus consumers unregistered");
              multiCompletePromise.complete();
            });

    AtomicInteger idx = new AtomicInteger(0);
    for (AutoCloseable service : closeables) {
      try {
        System.err.printf("closing: [%d/%d]%n", idx.incrementAndGet(), closeables.size());
        service.close();
      } catch (Exception e) {
        System.err.println("unable to close resources: " + e);
      }
    }

    System.err.println("awaitTermination...start");
    FutureUtil.awaitTermination()
        .onComplete(
            ar -> {
              System.err.printf("awaitTermination...end: %b%n", ar.result());
              multiCompletePromise.complete();
            });
  }
}
