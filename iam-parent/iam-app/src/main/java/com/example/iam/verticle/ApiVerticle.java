/* Licensed under Apache-2.0 2023. */
package com.example.iam.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.commons.auth.NoAuthRequiredAuthenticationProvider;
import com.example.commons.config.Config;
import com.example.commons.future.FutureUtil;
import com.example.iam.service.ServiceLifecycleManagement;
import com.example.iam.web.route.handler.UserHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.redis.client.RedisAPI;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;

public class ApiVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(ApiVerticle.class);

  private final ServiceLifecycleManagement serviceLifecycleManagement;
  private final UserHandler userHandler;
  private final RedisAPI redisAPI;
  private final Config config;

  @Inject
  ApiVerticle(
      ServiceLifecycleManagement serviceLifecycleManagement,
      UserHandler userHandler,
      RedisAPI redisAPI,
      Config config) {
    this.serviceLifecycleManagement = serviceLifecycleManagement;
    this.userHandler = userHandler;
    this.redisAPI = redisAPI;
    this.config = config;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.exceptionHandler(err -> log.error("unhandled exception", err));

    Router mainRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    mainRouter
        .route()
        // CORS config
        .handler(CorsHandler.create())
        // 100kB max body size
        .handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    // main routes
    mainRouter.route("/api/*").subRouter(apiRouter);

    // api routes
    apiRouter.post("/login").handler(userHandler::login);
    apiRouter.post("/refresh").handler(userHandler::refresh);
    apiRouter.post("/register").handler(userHandler::register);

    mainRouter.get("/health*").handler(getHealthCheckHandler());
    mainRouter.get("/ping*").handler(getPingHandler());

    // all unmatched requests go here
    mainRouter.route("/*").handler(ctx -> ctx.response().setStatusCode(NOT_FOUND.code()).end());

    log.info("ping redis before starting http server");
    redisAPI
        .ping(List.of())
        .onFailure(startPromise::fail)
        .onSuccess(
            ignore -> {
              Config.HttpConfig httpConfig = config.httpConfig();
              log.info("starting api verticle on port: " + httpConfig.port());
              vertx
                  .createHttpServer(
                      new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
                  .requestHandler(mainRouter)
                  .listen(
                      res -> {
                        if (res.succeeded()) {
                          log.info("started http server");
                          startPromise.complete();
                        } else {
                          log.error("failed to start verticle", res.cause());
                          startPromise.fail(res.cause());
                        }
                      });
            });
  }

  private HealthCheckHandler getPingHandler() {
    return HealthCheckHandler.create(vertx, NoAuthRequiredAuthenticationProvider.create())
        .register("ping", promise -> promise.complete(Status.OK()));
  }

  private HealthCheckHandler getHealthCheckHandler() {
    return HealthCheckHandler.create(vertx, NoAuthRequiredAuthenticationProvider.create())
        .register(
            "redis",
            Duration.ofSeconds(5L).toMillis(),
            promise -> {
              log.info("doing redis health check");
              redisAPI
                  .ping(List.of())
                  .onSuccess(r -> promise.complete(Status.OK()))
                  .onFailure(err -> promise.complete(Status.KO()));
            });
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void stop(Promise<Void> stopPromise) {
    System.err.println("stopping");

    Set<AutoCloseable> closeables = serviceLifecycleManagement.closeables();
    System.err.printf("closing created resources [%d]...%n", closeables.size());

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
              stopPromise.complete();
            });
  }
}
