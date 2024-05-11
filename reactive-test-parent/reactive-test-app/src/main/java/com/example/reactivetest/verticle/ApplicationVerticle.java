/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.commons.closer.ClosingService;
import com.example.commons.config.Config;
import com.example.commons.future.FutureUtil;
import com.example.commons.security.SecurityHandler;
import com.example.reactivetest.web.handler.PersonHandler;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(ApplicationVerticle.class);

  private final ClosingService closingService;
  private final PersonHandler personHandler;
  private final Config config;

  @Inject
  ApplicationVerticle(ClosingService closingService, PersonHandler personHandler, Config config) {
    this.closingService = closingService;
    this.personHandler = personHandler;
    this.config = config;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.exceptionHandler(err -> log.error("unhandled exception", err));
    log.info("starting");
    createRoutes(startPromise);
  }

  void createRoutes(Promise<Void> startPromise) {
    Router mainRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);
    Router personRouter = Router.router(vertx);

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

    // person routes
    apiRouter.route("/persons/*").subRouter(personRouter);
    personHandler.configureRoutes(personRouter);

    // https://vertx.io/docs/vertx-health-check/java/
    mainRouter
        .get("/health*")
        .handler(HealthCheckHandler.createWithHealthChecks(HealthChecks.create(vertx)));

    // all unmatched requests go here
    mainRouter.route("/*").handler(ctx -> ctx.response().setStatusCode(NOT_FOUND.code()).end());

    vertx.exceptionHandler(err -> log.error("unhandled exception", err));

    Config.HttpConfig httpConfig = config.httpConfig();
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

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void stop(Promise<Void> stopPromise) {
    System.err.println("stopping");

    Set<AutoCloseable> closeables = closingService.closeables();
    System.err.printf("closing created resources [%d]...%n", closeables.size());

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
              stopPromise.complete();
            });
  }
}
