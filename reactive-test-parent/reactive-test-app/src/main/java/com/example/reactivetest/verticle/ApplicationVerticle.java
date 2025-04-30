/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.reactivetest.web.handler.PersonHandler;
import github.benslabbert.vertxdaggercommons.closer.ClosingService;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.future.FutureUtil;
import github.benslabbert.vertxdaggercommons.future.MultiCompletePromise;
import github.benslabbert.vertxdaggercommons.security.SecurityHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.AuthorizationHandler;
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

  private HttpServer httpServer;

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
              AuthenticationProvider ap =
                  credentials -> {
                    User user =
                        User.create(
                            new JsonObject().put("username", "test"),
                            new JsonObject().put("attr-1", "value"));
                    return Future.succeededFuture(user);
                  };

              ap.authenticate(null);
            })
        .handler(
            AuthorizationHandler.create(RoleBasedAuthorization.create("my-role"))
                .addAuthorizationProvider(
                    new AuthorizationProvider() {
                      @Override
                      public String getId() {
                        return "role-provider-id";
                      }

                      @Override
                      public Future<Void> getAuthorizations(User user) {
                        user.authorizations()
                            .put("role-provider-id", RoleBasedAuthorization.create("my-role"));
                        return Future.succeededFuture();
                      }
                    }));

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

    mainRouter
        .get("/health*")
        .handler(
            ctx ->
                getHealthCheckHandler()
                    .checkStatus()
                    .onComplete(r -> ctx.response().write(r.result().getData().toBuffer())));

    // all unmatched requests go here
    mainRouter.route("/*").handler(ctx -> ctx.response().setStatusCode(NOT_FOUND.code()).end());

    vertx.exceptionHandler(err -> log.error("unhandled exception", err));

    Config.HttpConfig httpConfig = config.httpConfig();
    vertx
        .createHttpServer(new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
        .requestHandler(mainRouter)
        .listen()
        .onComplete(
            res -> {
              if (res.succeeded()) {
                log.info("started http server");
                httpServer = res.result();
                startPromise.complete();
              } else {
                log.error("failed to start verticle", res.cause());
                startPromise.fail(res.cause());
              }
            });
  }

  private HealthChecks getHealthCheckHandler() {
    return HealthChecks.create(vertx)
        .register("available", promise -> promise.complete(Status.OK()));
  }

  public int getPort() {
    return httpServer.actualPort();
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

    MultiCompletePromise multiCompletePromise = MultiCompletePromise.create(stopPromise, 2);

    httpServer.close().onComplete(multiCompletePromise::complete);

    System.err.println("awaitTermination...start");
    FutureUtil.awaitTermination()
        .onComplete(
            ar -> {
              System.err.printf("awaitTermination...end: %b%n", ar.result());
              multiCompletePromise.complete();
            });
  }
}
