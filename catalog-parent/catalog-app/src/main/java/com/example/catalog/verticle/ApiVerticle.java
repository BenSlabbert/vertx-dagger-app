/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.catalog.service.ItemService;
import com.example.catalog.web.route.handler.ItemHandler;
import com.example.commons.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
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
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.java.Log;
import org.apache.commons.lang3.math.NumberUtils;

@Log
public class ApiVerticle extends AbstractVerticle {

  private static final Pattern UUID_REGEX =
      Pattern.compile(
          "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

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
    mainRouter.route().handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    // main routes
    mainRouter.route("/api/*").subRouter(apiRouter);

    // api routes
    apiRouter
        .get("/items")
        .handler(
            ctx ->
                processWithPaginationParams(
                    ctx, (from, to, direction) -> itemHandler.findAll(ctx, from, to, direction)));

    apiRouter.post("/create").handler(itemHandler::create);

    apiRouter
        .get("/search")
        .handler(
            ctx ->
                processWithRequiredSearchParam(
                    ctx,
                    (s, i1, i2, i3, i4, i5) -> itemHandler.search(ctx, s, i1, i2, i3, i4, i5)));

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

  private void processWithRequiredIdParam(RoutingContext ctx, Consumer<UUID> consumer) {
    String param = ctx.pathParam("id");

    // do some sanity checks to avoid throwing expensive exception
    if (null == param
        || param.length() != 36
        // expensive regex check for last resort
        || !UUID_REGEX.matcher(param).matches()) {
      log.warning("path param id is not a uuid");
      ctx.fail(new HttpException(BAD_REQUEST.code()));
      return;
    }

    try {
      UUID id = UUID.fromString(param);
      consumer.accept(id);
    } catch (IllegalArgumentException e) {
      log.warning("path param id is not a uuid");
      ctx.fail(new HttpException(BAD_REQUEST.code()));
    }
  }

  @FunctionalInterface
  interface PentaConsumer<A, B, C, D, E, F> {
    void accept(A a, B b, C c, D d, E e, F f);
  }

  private void processWithRequiredSearchParam(
      RoutingContext ctx,
      PentaConsumer<String, Integer, Integer, ItemService.Direction, Long, Integer> consumer) {
    MultiMap entries = ctx.queryParams();
    String search = entries.get("s");

    Optional<Integer> priceFrom = tryParseInteger(entries.get("priceFrom"));
    Optional<Integer> priceTo = tryParseInteger(entries.get("priceTo"));
    ItemService.Direction direction = getDirection(entries);
    Optional<Integer> size = tryParseInteger(entries.get("size"));
    Optional<Long> lastId = tryParseLong(entries.get("lastId"));

    if (size.isEmpty()) {
      ctx.fail(new HttpException(BAD_REQUEST.code()));
      return;
    }

    if (priceFrom.isEmpty() || priceTo.isEmpty()) {
      ctx.fail(new HttpException(BAD_REQUEST.code()));
      return;
    }

    consumer.accept(
        search,
        priceFrom.get(),
        priceTo.get(),
        direction,
        lastId.orElse(0L),
        size.get() == 0 ? 10 : size.get());
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

  @FunctionalInterface
  interface TriConsumer<A, B, C> {
    void accept(A a, B b, C c);
  }

  private void processWithPaginationParams(
      RoutingContext ctx, TriConsumer<Long, Integer, ItemService.Direction> consumer) {
    MultiMap entries = ctx.queryParams();
    ItemService.Direction direction = getDirection(entries);

    Optional<Long> lastId = tryParseLong(entries.get("lastId"));
    Optional<Integer> size = tryParseInteger(entries.get("size"));

    consumer.accept(lastId.orElse(0L), size.orElse(0) == 0 ? 10 : size.get(), direction);
  }

  private ItemService.Direction getDirection(MultiMap entries) {
    String directionQuery =
        Optional.ofNullable(entries.get("direction")).orElse(ItemService.Direction.FORWARD.name());

    return switch (directionQuery.toUpperCase()) {
      case "BACKWARD" -> ItemService.Direction.BACKWARD;
      default -> ItemService.Direction.FORWARD;
    };
  }

  private Optional<Integer> tryParseInteger(String maybeInt) {
    if (null == maybeInt || maybeInt.isEmpty()) {
      return Optional.of(0);
    }

    if (NumberUtils.isCreatable(maybeInt)) {
      return Optional.of(Integer.parseInt(maybeInt));
    }

    return Optional.empty();
  }

  private Optional<Long> tryParseLong(String maybeInt) {
    if (null == maybeInt || maybeInt.isEmpty()) {
      return Optional.of(0L);
    }

    if (NumberUtils.isCreatable(maybeInt)) {
      return Optional.of(Long.parseLong(maybeInt));
    }

    return Optional.empty();
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    log.warning("stopping");
    stopPromise.complete();
  }
}
