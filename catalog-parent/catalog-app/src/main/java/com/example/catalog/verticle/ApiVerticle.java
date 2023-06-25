package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

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
import java.util.function.BiConsumer;
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
                processWithPaginationParams(ctx, (from, to) -> itemHandler.findAll(ctx, from, to)));

    apiRouter.post("/create").handler(itemHandler::create);

    apiRouter
        .get("/search")
        .handler(
            ctx ->
                processWithRequiredSearchParam(
                    ctx, (s, i1, i2, i3, i4) -> itemHandler.search(ctx, s, i1, i2, i3, i4)));

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
        || "".equals(param)
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
  interface PentaConsumer<A, B, C, D, E> {
    void accept(A a, B b, C c, D d, E e);
  }

  private void processWithRequiredSearchParam(
      RoutingContext ctx, PentaConsumer<String, Integer, Integer, Integer, Integer> consumer) {
    MultiMap entries = ctx.queryParams();
    String search = entries.get("s");

    Optional<Integer> priceFrom = tryParseInteger(entries.get("priceFrom"));
    Optional<Integer> priceTo = tryParseInteger(entries.get("priceTo"));
    Optional<Integer> page = tryParseInteger(entries.get("page"));
    Optional<Integer> size = tryParseInteger(entries.get("size"));

    if (page.isEmpty() || size.isEmpty()) {
      ctx.fail(new HttpException(BAD_REQUEST.code()));
      return;
    }

    if (priceFrom.isEmpty() || priceTo.isEmpty()) {
      ctx.fail(new HttpException(BAD_REQUEST.code()));
      return;
    }

    consumer.accept(
        search, priceFrom.get(), priceTo.get(), page.get(), size.get() == 0 ? 10 : size.get());
  }

  private void processWithPaginationParams(
      RoutingContext ctx, BiConsumer<Integer, Integer> consumer) {

    MultiMap entries = ctx.queryParams();
    Optional<Integer> page = tryParseInteger(entries.get("page"));
    Optional<Integer> size = tryParseInteger(entries.get("size"));

    if (page.isEmpty() || size.isEmpty()) {
      ctx.fail(new HttpException(BAD_REQUEST.code()));
      return;
    }

    consumer.accept(page.get(), size.get() == 0 ? 10 : size.get());
  }

  private Optional<Integer> tryParseInteger(String maybeInt) {
    if (null == maybeInt || "".equals(maybeInt)) {
      return Optional.of(0);
    }

    if (NumberUtils.isCreatable(maybeInt)) {
      return Optional.of(Integer.parseInt(maybeInt));
    }

    return Optional.empty();
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    log.warning("stopping");
    stopPromise.complete();
  }
}
