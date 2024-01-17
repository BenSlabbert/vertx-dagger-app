/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.catalog.ioc.DaggerProvider;
import com.example.catalog.ioc.Provider;
import com.example.catalog.web.route.handler.ItemHandler;
import com.example.commons.config.Config;
import com.example.commons.config.ParseConfig;
import com.example.commons.future.FutureUtil;
import com.example.commons.future.MultiCompletePromise;
import com.example.commons.mesage.Consumer;
import com.example.commons.web.IntegerParser;
import com.example.commons.web.LongParser;
import com.example.commons.web.RequestParser;
import com.example.commons.web.StringParser;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.HttpException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ApiVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(ApiVerticle.class);

  private Provider dagger;
  private Set<Consumer> consumers = Set.of();

  private void init() {
    log.info("ApiVerticle constructor");
    JsonObject cfg = config();
    Config config = ParseConfig.get(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(config);
    Objects.requireNonNull(config.postgresConfig());
    Objects.requireNonNull(config.httpConfig());
    Objects.requireNonNull(config.redisConfig());
    Objects.requireNonNull(config.verticleConfig());
    Objects.requireNonNull(config.serviceRegistryConfig());

    this.dagger =
        DaggerProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .redisConfig(config.redisConfig())
            .verticleConfig(config.verticleConfig())
            .serviceRegistryConfig(config.serviceRegistryConfig())
            .postgresConfig(config.postgresConfig())
            .build();

    this.dagger.init();
  }

  @Override
  public void start(Promise<Void> startPromise) {
    init();
    log.info("starting ApiVerticle");

    checkConnections(startPromise)
        .onSuccess(
            ignore -> {
              Config.HttpConfig httpConfig = dagger.config().httpConfig();
              log.info("starting api verticle on port: " + httpConfig.port());

              vertx
                  .createHttpServer(
                      new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
                  .requestHandler(setupRoutes())
                  .listen(
                      res -> {
                        if (res.succeeded()) {
                          log.info("started http server");
                          log.info("register consumers");
                          consumers = dagger.consumers();
                          consumers.forEach(Consumer::register);
                          startPromise.complete();
                        } else {
                          log.error("failed to start verticle", res.cause());
                          startPromise.fail(res.cause());
                        }
                      });
            });
  }

  private Router setupRoutes() {
    Router mainRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    mainRouter
        .route()
        // CORS config
        .handler(CorsHandler.create())
        // auth handler
        .handler(dagger.authHandler());

    // main routes
    mainRouter
        .route("/api/*")
        // 100kB max body size
        .handler(BodyHandler.create().setBodyLimit(1024L * 100L))
        .subRouter(apiRouter);

    ItemHandler itemHandler = dagger.itemHandler();

    // api routes
    apiRouter.post("/execute").handler(itemHandler::execute);

    apiRouter
        .get("/items")
        .handler(
            ctx -> {
              RequestParser rp = RequestParser.create(ctx);
              Long lastId = rp.getQueryParam("lastId", 0L, LongParser.create());
              Integer size = rp.getQueryParam("size", 10, IntegerParser.create());
              itemHandler.findAll(ctx, new ItemHandler.FindAllRequestDto(lastId, size));
            });

    apiRouter.post("/create").handler(itemHandler::create);

    apiRouter
        .get("/suggest")
        .handler(
            ctx -> {
              RequestParser rp = RequestParser.create(ctx);
              String s = rp.getQueryParam("s", "", StringParser.create());
              if (null == s || s.isEmpty()) {
                ctx.fail(new HttpException(BAD_REQUEST.code()));
                return;
              }

              itemHandler.suggest(ctx, s);
            });

    apiRouter
        .get("/:id")
        .handler(
            ctx -> {
              RequestParser rp = RequestParser.create(ctx);
              Long id = rp.getPathParam("id", LongParser.create());

              if (null == id) {
                ctx.fail(new HttpException(BAD_REQUEST.code()));
                return;
              }

              itemHandler.findOne(ctx, id);
            });

    apiRouter
        .delete("/:id")
        .handler(
            ctx -> {
              RequestParser rp = RequestParser.create(ctx);
              Long id = rp.getPathParam("id", LongParser.create());

              if (null == id) {
                ctx.fail(new HttpException(BAD_REQUEST.code()));
                return;
              }

              itemHandler.deleteOne(ctx, id);
            });

    apiRouter
        .post("/edit/:id")
        .handler(
            ctx -> {
              RequestParser rp = RequestParser.create(ctx);
              Long id = rp.getPathParam("id", LongParser.create());

              if (null == id) {
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
    return mainRouter;
  }

  private Future<?> checkConnections(Promise<Void> startPromise) {
    return checkDbConnection(startPromise);
  }

  private Future<?> checkDbConnection(Promise<Void> startPromise) {
    return dagger
        .pool()
        .getConnection()
        .onFailure(startPromise::fail)
        .onSuccess(
            conn ->
                conn.close()
                    .onFailure(startPromise::fail)
                    .map(ignore -> checkRedisConnection(startPromise)));
  }

  private Future<?> checkRedisConnection(Promise<Void> startPromise) {
    return dagger.redisAPI().ping(List.of()).onFailure(startPromise::fail);
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void stop(Promise<Void> stopPromise) {
    System.err.println("stopping");
    MultiCompletePromise multiCompletePromise = MultiCompletePromise.create(stopPromise, 2);

    Set<AutoCloseable> closeables = dagger.providesServiceLifecycleManagement().closeables();
    System.err.printf("closing created resources [%d]...%n", closeables.size());

    Future.all(consumers.stream().map(Consumer::unregister).toList())
        .onComplete(
            ar -> {
              System.err.println("all eventbus consumers unregistered");
              multiCompletePromise.complete();
            });

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
              multiCompletePromise.complete();
            });
  }
}
