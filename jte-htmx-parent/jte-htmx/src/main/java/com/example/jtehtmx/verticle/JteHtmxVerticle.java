/* Licensed under Apache-2.0 2024. */
package com.example.jtehtmx.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import com.example.jtehtmx.web.handler.ExampleHandler;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.future.FutureUtil;
import github.benslabbert.vertxdaggercommons.future.MultiCompletePromise;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JteHtmxVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(JteHtmxVerticle.class);

  private final ExampleHandler exampleHandler;
  private final Config config;

  private HttpServer server;

  @Inject
  JteHtmxVerticle(Config config, ExampleHandler exampleHandler) {
    this.exampleHandler = exampleHandler;
    this.config = config;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.exceptionHandler(err -> log.error("unhandled exception", err));

    Router mainRouter = Router.router(vertx);

    mainRouter
        .route()
        // CORS config
        .handler(CorsHandler.create())
        // 100kB max body size
        .handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    mainRouter
        .route("/api/*")
        .handler(
            ctx -> {
              HttpMethod method = ctx.request().method();
              String path = ctx.request().path();
              String query = ctx.request().query();

              log.info("handle api request");
              if (null == query) {
                log.info("[{}] {}", method, path);
              } else {
                log.info("[{}] {}?{}", method, path, query);
              }

              MultiMap parsedHeaderValues = ctx.request().headers();
              String htmxRequest = parsedHeaderValues.get("HX-Request");

              if (!Boolean.parseBoolean(htmxRequest)) {
                log.warn("request is not an htmx request");
                ctx.fail(BAD_REQUEST.code());
                return;
              }

              ctx.next();
            });

    StaticHandler staticHandler = StaticHandler.create("svelte");

    mainRouter
        .get()
        .handler(
            ctx -> {
              String path = ctx.request().path();

              if (path.startsWith("/api")
                  || path.startsWith("/health")
                  || path.startsWith("/ping")) {
                ctx.next();
                return;
              }
              if (path.equals("/") || path.endsWith(".js") || path.endsWith(".css")) {
                staticHandler.handle(ctx);
                return;
              }

              vertx
                  .fileSystem()
                  .readFile("svelte/index.html")
                  .onComplete(
                      ar -> {
                        if (ar.failed()) {
                          ctx.response().setStatusCode(NOT_FOUND.code()).end();
                          return;
                        }

                        ctx.response().setStatusCode(OK.code()).end(ar.result());
                      });
            });

    exampleHandler.configureRoutes(mainRouter);

    Config.HttpConfig httpConfig = config.httpConfig();
    log.info("starting api verticle on port: {}", httpConfig.port());
    vertx
        .createHttpServer(new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
        .requestHandler(mainRouter)
        .listen()
        .onComplete(
            res -> {
              if (res.succeeded()) {
                log.info("started http server");
                server = res.result();
                startPromise.complete();
              } else {
                log.error("failed to start verticle", res.cause());
                startPromise.fail(res.cause());
              }
            });
  }

  public int getPort() {
    return server.actualPort();
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void stop(Promise<Void> stopPromise) {
    System.err.println("stopping");
    System.err.println("awaitTermination...start");

    MultiCompletePromise multiCompletePromise = MultiCompletePromise.create(stopPromise, 2);
    if (null == server) {
      multiCompletePromise.complete();
    } else {
      server.close().onComplete(multiCompletePromise::complete);
    }

    FutureUtil.awaitTermination()
        .onComplete(
            ar -> {
              System.err.printf("awaitTermination...end: %b%n", ar.result());
              stopPromise.complete();
            });
  }
}
