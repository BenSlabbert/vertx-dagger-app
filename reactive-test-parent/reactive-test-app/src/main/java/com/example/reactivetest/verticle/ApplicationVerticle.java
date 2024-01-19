/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.commons.config.Config;
import com.example.commons.config.ParseConfig;
import com.example.reactivetest.ioc.DaggerProvider;
import com.example.reactivetest.ioc.Provider;
import com.example.reactivetest.web.handler.PersonHandler;
import com.example.reactivetest.web.handler.SecurityHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Objects;

public class ApplicationVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(ApplicationVerticle.class);

  private Provider dagger;

  @Override
  public void start(Promise<Void> startPromise) {
    log.info("starting");
    init();
    createRoutes(startPromise);
  }

  private void init() {
    JsonObject cfg = config();
    Config config = ParseConfig.get(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(config);

    log.info("create dagger");
    this.dagger = DaggerProvider.builder().vertx(vertx).config(config).build();
    log.info("init dagger deps");
    this.dagger.init();
    log.info("dagger init complete");
  }

  void createRoutes(Promise<Void> startPromise) {
    Router mainRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    mainRouter
        .route()
        .handler(
            ctx -> {
              // authentication start
              ctx.setUser(
                  User.create(
                      new JsonObject().put("username", "test"),
                      new JsonObject().put("attr-1", "value")));
              User user = ctx.user();
              JsonObject principal = user.principal();
              JsonObject attributes = user.attributes();
              log.info("principal: " + principal);
              log.info("attributes: " + attributes);
              // add user roles
              user.authorizations()
                  .add("role-provider-id", RoleBasedAuthorization.create("my-role"));

              ctx.next();
            });

    // 100kB max body size
    mainRouter
        .route(HttpMethod.POST, "/*")
        .handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    // main routes
    mainRouter.route("/api/*").subRouter(apiRouter);

    // check permissions
    apiRouter
        .route()
        .handler(ctx -> SecurityHandler.hasRole(ctx, RoleBasedAuthorization.create("my-role")));

    // api routes
    PersonHandler personHandler = dagger.personHandler();
    apiRouter.get("/persons/all").handler(personHandler::getAll);
    apiRouter.post("/persons/create").handler(personHandler::create);
    apiRouter.get("/persons/sse").handler(personHandler::sse);

    // https://vertx.io/docs/vertx-health-check/java/
    mainRouter
        .get("/health*")
        .handler(HealthCheckHandler.createWithHealthChecks(HealthChecks.create(vertx)));

    // all unmatched requests go here
    mainRouter.route("/*").handler(ctx -> ctx.response().setStatusCode(NOT_FOUND.code()).end());

    vertx.exceptionHandler(err -> log.error("unhandled exception", err));

    Config.HttpConfig httpConfig = dagger.config().httpConfig();
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

  @Override
  public void stop(Promise<Void> stopPromise) {
    log.warn("stopping");
    stopPromise.complete();
  }
}
