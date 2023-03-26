package com.example.starter.verticle;

import com.example.starter.route.handler.PingHandler;
import com.example.starter.route.handler.UserHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
public class ApiVerticle extends AbstractVerticle {

  private final UserHandler userHandler;

  @Inject
  public ApiVerticle(UserHandler userHandler) {
    this.userHandler = userHandler;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    log.info("starting");

    HttpServer server = vertx.createHttpServer();
    Router mainRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    // 100kB max body size
    mainRouter.route().handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    // main routes
    mainRouter.get("/ping").handler(new PingHandler());
    mainRouter.route("/api/*").subRouter(apiRouter);

    // api routes
    apiRouter.post("/login").handler(userHandler::login);
    apiRouter.post("/refresh").handler(userHandler::refresh);
    apiRouter.post("/register").handler(userHandler::register);

    server
        .requestHandler(mainRouter)
        .listen(
            8080,
            res -> {
              if (res.succeeded()) {
                log.info("started http server");
                startPromise.complete();
              } else {
                startPromise.fail(res.cause());
              }
            });
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    log.info("stopping");
    stopPromise.complete();
  }
}
