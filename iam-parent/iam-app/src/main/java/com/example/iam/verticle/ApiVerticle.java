/* Licensed under Apache-2.0 2023. */
package com.example.iam.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.commons.config.Config;
import com.example.iam.web.route.handler.UserHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.logging.Level;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
public class ApiVerticle extends AbstractVerticle {

  private final UserHandler userHandler;
  private final Config.HttpConfig httpConfig;

  @Inject
  public ApiVerticle(UserHandler userHandler, Config.HttpConfig httpConfig) {
    this.userHandler = userHandler;
    this.httpConfig = httpConfig;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    log.log(
        Level.INFO,
        "starting api verticle on port: {0}",
        new Object[] {Integer.toString(httpConfig.port())});

    Router mainRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    // 100kB max body size
    mainRouter
        .route(HttpMethod.POST, "/*")
        .handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    // main routes
    mainRouter.route("/api/*").subRouter(apiRouter);

    // api routes
    apiRouter.post("/login").handler(userHandler::login);
    apiRouter.post("/refresh").handler(userHandler::refresh);
    apiRouter.post("/register").handler(userHandler::register);

    // https://vertx.io/docs/vertx-health-check/java/
    mainRouter
        .get("/health*")
        .handler(HealthCheckHandler.createWithHealthChecks(HealthChecks.create(vertx)));

    // all unmatched requests go here
    mainRouter.route("/*").handler(ctx -> ctx.response().setStatusCode(NOT_FOUND.code()).end());

    vertx
        .createHttpServer(new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
        .requestHandler(mainRouter)
        .listen(
            res -> {
              if (res.succeeded()) {
                log.info("started http server");
                startPromise.complete();
              } else {
                log.log(Level.SEVERE, "failed to start verticle", res.cause());
                startPromise.fail(res.cause());
              }
            });
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    log.warning("stopping");
    stopPromise.complete();
  }
}
