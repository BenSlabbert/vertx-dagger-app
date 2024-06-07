/* Licensed under Apache-2.0 2024. */
package com.example.jtehtmx.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

import com.example.jtehtmx.web.handler.ExampleHandler;
import github.benslabbert.vertxdaggercommons.auth.NoAuthRequiredAuthenticationProvider;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.future.FutureUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
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

    mainRouter.get("/health*").handler(getHealthCheckHandler());
    mainRouter.get("/ping*").handler(getPingHandler());
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

    mainRouter
        .route("/*")
        .handler(
            ctx -> {
              // if the request if for js or css, send the requested file
              String path = ctx.request().path();

              if (path.startsWith("/api")
                  || path.startsWith("/health")
                  || path.startsWith("/ping")) {
                ctx.next();
                return;
              }

              StaticHandler.create("svelte").handle(ctx);
            });
    exampleHandler.configureRoutes(mainRouter);

    Config.HttpConfig httpConfig = config.httpConfig();
    log.info("starting api verticle on port: {}", httpConfig.port());
    vertx
        .createHttpServer(new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
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
  }

  private HealthCheckHandler getPingHandler() {
    return HealthCheckHandler.create(vertx, NoAuthRequiredAuthenticationProvider.create())
        .register("ping", promise -> promise.complete(Status.OK()));
  }

  private HealthCheckHandler getHealthCheckHandler() {
    return HealthCheckHandler.create(vertx, NoAuthRequiredAuthenticationProvider.create())
        .register("available", promise -> promise.complete(Status.OK()));
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void stop(Promise<Void> stopPromise) {
    System.err.println("stopping");
    System.err.println("awaitTermination...start");
    FutureUtil.awaitTermination()
        .onComplete(
            ar -> {
              System.err.printf("awaitTermination...end: %b%n", ar.result());
              stopPromise.complete();
            });
  }
}
