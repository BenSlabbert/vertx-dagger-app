/* Licensed under Apache-2.0 2024. */
package com.example.vt.verticle;

import com.example.vt.handler.RequestHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(DefaultVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    router.errorHandler(
        HttpResponseStatus.REQUEST_TIMEOUT.code(),
        ctx -> log.error("code error handler", ctx.failure()));
    new RequestHandler().configure(router);

    vertx.exceptionHandler(err -> log.error("vertx error", err));

    vertx
        .createHttpServer(new HttpServerOptions().setPort(8080).setHost("0.0.0.0"))
        .exceptionHandler(err -> log.error("http server error", err))
        .requestHandler(router)
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
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    stopPromise.complete();
  }
}
