/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.commons.config.Config;
import com.example.reactivetest.web.handler.PersonHandler;
import com.example.reactivetest.web.handler.SecurityHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.logging.Level;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
public class ApplicationVerticle extends AbstractVerticle {

  private final PersonHandler personHandler;
  private final Config.HttpConfig httpConfig;

  @Inject
  ApplicationVerticle(Config config, PersonHandler personHandler) {
    this.personHandler = personHandler;
    this.httpConfig = config.httpConfig();
  }

  @Override
  public void start(Promise<Void> startPromise) {
    log.log(
        Level.INFO,
        "starting api verticle on port: {0}",
        new Object[] {Integer.toString(httpConfig.port())});

    createRoutes(startPromise);
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
    apiRouter.get("/persons/all").handler(personHandler::getAll);
    apiRouter.post("/persons/create").handler(personHandler::create);
    apiRouter.get("/persons/sse").handler(personHandler::sse);

    // https://vertx.io/docs/vertx-health-check/java/
    mainRouter
        .get("/health*")
        .handler(HealthCheckHandler.createWithHealthChecks(HealthChecks.create(vertx)));

    // all unmatched requests go here
    mainRouter.route("/*").handler(ctx -> ctx.response().setStatusCode(NOT_FOUND.code()).end());

    vertx.exceptionHandler(err -> log.log(Level.SEVERE, "unhandled exception", err));

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
