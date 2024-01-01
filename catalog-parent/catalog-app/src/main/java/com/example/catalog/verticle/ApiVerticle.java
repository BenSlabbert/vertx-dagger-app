/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.catalog.web.route.handler.AuthHandler;
import com.example.catalog.web.route.handler.ItemHandler;
import com.example.commons.config.Config;
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
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Pool;
import java.util.List;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
public class ApiVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(ApiVerticle.class);

  private final AuthHandler authHandler;
  private final Config.HttpConfig httpConfig;
  private final ItemHandler itemHandler;
  private final Pool pool;
  private final RedisAPI redisAPI;

  @Override
  public void start(Promise<Void> startPromise) {
    checkConnections(startPromise)
        .onSuccess(
            ignore -> {
              log.info("starting api verticle on port: " + httpConfig.port());

              vertx
                  .createHttpServer(
                      new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
                  .requestHandler(setupRoutes())
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
        .handler(authHandler);

    // main routes
    mainRouter
        .route("/api/*")
        // 100kB max body size
        .handler(BodyHandler.create().setBodyLimit(1024L * 100L))
        .subRouter(apiRouter);

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
    return pool.getConnection()
        .onFailure(startPromise::fail)
        .onSuccess(
            conn ->
                conn.close()
                    .onFailure(startPromise::fail)
                    .map(ignore -> checkRedisConnection(startPromise)));
  }

  private Future<?> checkRedisConnection(Promise<Void> startPromise) {
    return redisAPI.ping(List.of()).onFailure(startPromise::fail);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    log.warn("stopping");
    stopPromise.complete();
  }
}
