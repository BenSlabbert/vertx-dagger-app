/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.catalog.web.route.handler.AuthHandler;
import com.example.catalog.web.route.handler.ItemHandler;
import com.example.commons.closer.ClosingService;
import com.example.commons.config.Config;
import com.example.commons.future.FutureUtil;
import com.example.commons.future.MultiCompletePromise;
import com.example.commons.mesage.Consumer;
import com.example.commons.security.SecurityHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Pool;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
public class ApiVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(ApiVerticle.class);

  private final ClosingService closingService;
  private final Set<Consumer> consumers;
  private final ItemHandler itemHandler;
  private final AuthHandler authHandler;
  private final RedisAPI redisAPI;
  private final Config config;
  private final Pool pool;

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.exceptionHandler(err -> log.error("unhandled exception", err));
    log.info("starting ApiVerticle");

    checkConnections(startPromise)
        .onSuccess(
            ignore -> {
              Config.HttpConfig httpConfig = config.httpConfig();
              log.info("starting api verticle on port: " + httpConfig.port());

              vertx
                  .createHttpServer(
                      new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
                  .requestHandler(setupRoutes())
                  .listen(
                      res -> {
                        if (res.succeeded()) {
                          log.info("started http server");
                          log.info("register consumers");
                          consumers.forEach(Consumer::register);
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

    // https://vertx.io/docs/vertx-health-check/java/
    mainRouter
        .get("/health*")
        .handler(HealthCheckHandler.createWithHealthChecks(HealthChecks.create(vertx)));

    // all unmatched requests go here
    mainRouter.route("/*").handler(ctx -> ctx.response().setStatusCode(NOT_FOUND.code()).end());
    return mainRouter;
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

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void stop(Promise<Void> stopPromise) {
    System.err.println("stopping");
    MultiCompletePromise multiCompletePromise = MultiCompletePromise.create(stopPromise, 2);

    Set<AutoCloseable> closeables = closingService.closeables();
    System.err.printf("closing created resources [%d]...%n", closeables.size());

    Future.all(consumers.stream().map(Consumer::unregister).toList())
        .onComplete(
            ar -> {
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
