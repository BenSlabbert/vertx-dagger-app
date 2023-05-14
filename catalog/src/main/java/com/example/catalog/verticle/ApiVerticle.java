package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.catalog.web.route.handler.ItemHandler;
import com.example.commons.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.HttpException;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
public class ApiVerticle extends AbstractVerticle {

  private final Config.HttpConfig httpConfig;
  private final ItemHandler itemHandler;
  private final Supplier<Handler<RoutingContext>> handlerSupplier;

  @Inject
  public ApiVerticle(
      Config.HttpConfig httpConfig,
      ItemHandler itemHandler,
      Supplier<Handler<RoutingContext>> handlerSupplier) {
    this.httpConfig = httpConfig;
    this.itemHandler = itemHandler;
    this.handlerSupplier = handlerSupplier;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    log.log(
        Level.INFO,
        "starting api verticle on port: {0}",
        new Object[] {Integer.toString(httpConfig.port())});

    Router mainRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    mainRouter.route().handler(ctx -> handlerSupplier.get().handle(ctx));

    // 100kB max body size
    mainRouter
        .route(HttpMethod.POST, "/*")
        .handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    // main routes
    mainRouter.route("/api/*").subRouter(apiRouter);

    // api routes
    apiRouter.get("/items").handler(itemHandler::findAll);
    apiRouter.post("/create").handler(itemHandler::create);

    apiRouter
        .get("/:id")
        .handler(
            ctx -> {
              log.info("get one request");
              UUID id;
              try {
                id = UUID.fromString(ctx.pathParam("id"));
              } catch (IllegalArgumentException e) {
                log.warning("path param id is not a uuid");
                ctx.fail(new HttpException(BAD_REQUEST.code()));
                return;
              }

              itemHandler.findOne(ctx, id);
            });

    apiRouter
        .delete("/:id")
        .handler(
            ctx -> {
              log.info("get one request");
              UUID id;
              try {
                id = UUID.fromString(ctx.pathParam("id"));
              } catch (IllegalArgumentException e) {
                log.warning("path param id is not a uuid");
                ctx.fail(new HttpException(BAD_REQUEST.code()));
                return;
              }

              itemHandler.deleteOne(ctx, id);
            });

    apiRouter
        .post("/edit/:id")
        .handler(
            ctx -> {
              log.info("edit request");
              UUID id;
              try {
                id = UUID.fromString(ctx.pathParam("id"));
              } catch (IllegalArgumentException e) {
                log.warning("path param id is not a uuid");
                ctx.fail(new HttpException(BAD_REQUEST.code()));
                return;
              }

              itemHandler.update(ctx, id);
            });

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
