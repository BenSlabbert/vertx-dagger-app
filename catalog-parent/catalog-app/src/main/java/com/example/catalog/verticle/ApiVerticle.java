/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.catalog.web.route.handler.AuthHandler;
import com.example.catalog.web.route.handler.ItemHandler;
import com.example.commons.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.HttpException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.logging.Level;
import javax.inject.Inject;
import lombok.extern.java.Log;
import org.apache.commons.lang3.math.NumberUtils;

@Log
public class ApiVerticle extends AbstractVerticle {

  private final AuthHandler authHandler;
  private final Config.HttpConfig httpConfig;
  private final ItemHandler itemHandler;

  @Inject
  public ApiVerticle(
      Config.HttpConfig httpConfig, ItemHandler itemHandler, AuthHandler authHandler) {
    this.httpConfig = httpConfig;
    this.itemHandler = itemHandler;
    this.authHandler = authHandler;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    log.log(
        Level.INFO,
        "starting api verticle on port: {0}",
        new Object[] {Integer.toString(httpConfig.port())});

    Router mainRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    mainRouter.route().handler(authHandler);

    // 100kB max body size
    mainRouter.route().handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    // main routes
    mainRouter.route("/api/*").subRouter(apiRouter);

    // api routes
    apiRouter
        .get("/items")
        .handler(
            ctx ->
                processWithPaginationParams(ctx, (from, to) -> itemHandler.findAll(ctx, from, to)));

    apiRouter.post("/create").handler(itemHandler::create);

    apiRouter
        .get("/suggest")
        .handler(ctx -> processWithRequiredSuggestParam(ctx, s -> itemHandler.suggest(ctx, s)));

    apiRouter
        .get("/:id")
        .handler(ctx -> processWithRequiredIdParam(ctx, id -> itemHandler.findOne(ctx, id)));

    apiRouter
        .delete("/:id")
        .handler(ctx -> processWithRequiredIdParam(ctx, id -> itemHandler.deleteOne(ctx, id)));

    apiRouter
        .post("/edit/:id")
        .handler(ctx -> processWithRequiredIdParam(ctx, id -> itemHandler.update(ctx, id)));

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

  private void processWithRequiredIdParam(RoutingContext ctx, LongConsumer consumer) {
    Optional<Long> id = parseLong(ctx.pathParam("id"));

    if (id.isEmpty()) {
      ctx.fail(new HttpException(BAD_REQUEST.code()));
      return;
    }

    consumer.accept(id.get());
  }

  private void processWithRequiredSuggestParam(RoutingContext ctx, Consumer<String> consumer) {
    MultiMap entries = ctx.queryParams();
    String search = entries.get("s");

    if (null == search || search.isEmpty()) {
      ctx.fail(new HttpException(BAD_REQUEST.code()));
      return;
    }

    consumer.accept(search);
  }

  private void processWithPaginationParams(RoutingContext ctx, BiConsumer<Long, Integer> consumer) {
    MultiMap entries = ctx.queryParams();

    Optional<Long> lastId = parseLong(entries.get("lastId"));
    Optional<Integer> size = parseInteger(entries.get("size"));

    consumer.accept(lastId.orElse(0L), size.orElse(10));
  }

  private Optional<Integer> parseInteger(String maybeInt) {
    if (null == maybeInt || maybeInt.isEmpty()) {
      return Optional.empty();
    }

    if (NumberUtils.isCreatable(maybeInt)) {
      return Optional.of(NumberUtils.createInteger(maybeInt));
    }

    return Optional.empty();
  }

  private Optional<Long> parseLong(String maybeInt) {
    if (null == maybeInt || maybeInt.isEmpty()) {
      return Optional.empty();
    }

    if (NumberUtils.isCreatable(maybeInt)) {
      return Optional.of(NumberUtils.createLong(maybeInt));
    }

    return Optional.empty();
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    log.warning("stopping");
    stopPromise.complete();
  }
}
