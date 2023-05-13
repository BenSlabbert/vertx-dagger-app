package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;

import com.example.catalog.web.route.handler.ItemHandler;
import com.example.commons.config.Config;
import com.example.iam.grpc.iam.CheckTokenRequest;
import com.example.iam.grpc.iam.IamGrpc;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.common.GrpcReadStream;
import java.util.logging.Level;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
public class ApiVerticle extends AbstractVerticle {

  private final Config.HttpConfig httpConfig;
  private final ItemHandler itemHandler;

  @Inject
  public ApiVerticle(Config.HttpConfig httpConfig, ItemHandler itemHandler) {
    this.httpConfig = httpConfig;
    this.itemHandler = itemHandler;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    log.log(
        Level.INFO,
        "starting api verticle on port: {0}",
        new Object[] {Integer.toString(httpConfig.port())});

    Router mainRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    GrpcClient client = GrpcClient.client(vertx);
    SocketAddress server = SocketAddress.inetSocketAddress(50051, "localhost");

    mainRouter.route().handler(authHandler(client, server));

    // 100kB max body size
    mainRouter
        .route(HttpMethod.POST, "/*")
        .handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    // main routes
    mainRouter.route("/api/*").subRouter(apiRouter);

    // api routes
    apiRouter.get("/").handler(itemHandler::findAll);
    apiRouter.post("/create").handler(itemHandler::create);

    apiRouter
        .get("/:id")
        .handler(
            ctx -> {
              log.info("get one request");
              long id;
              try {
                id = Long.parseLong(ctx.pathParam("id"));
              } catch (NumberFormatException e) {
                log.warning("path param id is not a number");
                ctx.fail(new HttpException(BAD_REQUEST.code()));
                ctx.end();
                return;
              }

              itemHandler.findOne(ctx, id);
            });

    apiRouter
        .delete("/:id")
        .handler(
            ctx -> {
              log.info("get one request");
              long id;
              try {
                id = Long.parseLong(ctx.pathParam("id"));
              } catch (NumberFormatException e) {
                log.warning("path param id is not a number");
                ctx.fail(new HttpException(BAD_REQUEST.code()));
                ctx.end();
                return;
              }

              itemHandler.deleteOne(ctx, id);
            });

    apiRouter
        .post("/edit/:id")
        .handler(
            ctx -> {
              log.info("edit request");
              long id;
              try {
                id = Long.parseLong(ctx.pathParam("id"));
              } catch (NumberFormatException e) {
                log.warning("path param id is not a number");
                ctx.fail(new HttpException(BAD_REQUEST.code()));
                ctx.end();
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
        .createHttpServer(new HttpServerOptions().setPort(8081).setHost("0.0.0.0"))
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

  private Handler<RoutingContext> authHandler(GrpcClient client, SocketAddress server) {
    return ctx -> {
      String authHeader = ctx.request().getHeader(HttpHeaders.AUTHORIZATION);

      if (null == authHeader) {
        ctx.fail(new HttpException(UNAUTHORIZED.code()));
        ctx.end();
        return;
      }

      client
          .request(server, IamGrpc.getCheckTokenMethod())
          .compose(
              request -> {
                request.end(CheckTokenRequest.newBuilder().setToken(authHeader).build());
                return request.response().compose(GrpcReadStream::last);
              })
          .onFailure(
              t -> {
                ctx.fail(new HttpException(UNAUTHORIZED.code()));
                ctx.end();
              })
          .onSuccess(
              reply -> {
                if (reply.getValid()) {
                  ctx.next();
                  return;
                }

                ctx.fail(new HttpException(UNAUTHORIZED.code()));
                ctx.end();
              });
    };
  }
}
