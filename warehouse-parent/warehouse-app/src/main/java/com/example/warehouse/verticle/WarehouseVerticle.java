/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.commons.auth.NoAuthRequiredAuthenticationProvider;
import com.example.commons.closer.ClosingService;
import com.example.commons.config.Config;
import com.example.commons.future.FutureUtil;
import com.example.commons.future.MultiCompletePromise;
import com.example.commons.transaction.reactive.TransactionBoundary;
import com.example.warehouse.rpc.api.WarehouseRpcService;
import com.example.warehouse.rpc.api.WarehouseRpcServiceVertxProxyHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.auth.jwt.authorization.JWTAuthorization;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.serviceproxy.AuthenticationInterceptor;
import io.vertx.serviceproxy.AuthorizationInterceptor;
import io.vertx.serviceproxy.impl.InterceptorHolder;
import io.vertx.sqlclient.Pool;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;

public class WarehouseVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(WarehouseVerticle.class);

  private final AuthenticationProvider iamRpcServiceAuthenticationProvider;
  private final ClosingService closingService;
  private final WarehouseRpcService warehouseRpcService;
  private final Config config;
  private final Pool pool;

  private MessageConsumer<JsonObject> consumer;

  @Inject
  WarehouseVerticle(
      AuthenticationProvider iamRpcServiceAuthenticationProvider,
      ClosingService closingService,
      WarehouseRpcService warehouseRpcService,
      Config config,
      Pool pool) {
    this.iamRpcServiceAuthenticationProvider = iamRpcServiceAuthenticationProvider;
    this.closingService = closingService;
    this.warehouseRpcService = warehouseRpcService;
    this.config = config;
    this.pool = pool;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.exceptionHandler(err -> log.error("unhandled exception", err));
    log.info("starting");

    vertx
        .eventBus()
        .addInboundInterceptor(
            ctx -> {
              log.info("inbound interceptor");
              ctx.next();
            })
        .addOutboundInterceptor(
            ctx -> {
              log.info("outbound interceptor");
              ctx.next();
            });

    var authenticationInterceptor =
        new InterceptorHolder(
            AuthenticationInterceptor.create(iamRpcServiceAuthenticationProvider));

    InterceptorHolder permissionsInterceptorHolder =
        new InterceptorHolder(
            (vertx, interceptorContext, body) -> {
              // we come here if the user is authenticated
              // user has a valid token
              final ContextInternal vertxContext = (ContextInternal) vertx.getOrCreateContext();
              User user = (User) interceptorContext.get("user");

              // jwt auth only does PermissionBasedAuthorization
              // we will need to add roles if we want to use them
              user.authorizations()
                  .add("role-provider-id", RoleBasedAuthorization.create("service-client"));

              log.info("user: " + user);
              return vertxContext.succeededFuture(body);
            });

    var roleInterceptor =
        new InterceptorHolder(
            // look for a claim called "permissions"
            AuthorizationInterceptor.create(JWTAuthorization.create("permissions"))
                // this authorization is added above in the permissionsInterceptorHolder
                .addAuthorization(RoleBasedAuthorization.create("service-client"))
                // one of the permissions must be called "truck-client"
                // this is coming off the root claim in the JWT token
                .addAuthorization(PermissionBasedAuthorization.create("truck-client")));

    this.consumer =
        new WarehouseRpcServiceVertxProxyHandler(vertx, warehouseRpcService)
            .register(
                vertx,
                WarehouseRpcService.ADDRESS,
                List.of(authenticationInterceptor, permissionsInterceptorHolder, roleInterceptor))
            .setMaxBufferedMessages(100)
            .fetch(10)
            .exceptionHandler(err -> log.error("exception in event bus", err))
            .endHandler(ignore -> log.info("end handler"));

    Router mainRouter = Router.router(vertx);

    mainRouter
        .route()
        // CORS config
        .handler(CorsHandler.create())
        // 100kB max body size
        .handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    // main routes
    mainRouter.get("/health*").handler(getHealthCheckHandler());
    mainRouter.get("/ping*").handler(getPingHandler());

    // all unmatched requests go here
    mainRouter.route("/*").handler(ctx -> ctx.response().setStatusCode(NOT_FOUND.code()).end());

    Config.HttpConfig httpConfig = config.httpConfig();
    log.info("starting on port: " + httpConfig.port());
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

  private HealthCheckHandler getPingHandler() {
    return HealthCheckHandler.create(vertx, NoAuthRequiredAuthenticationProvider.create())
        .register("ping", promise -> promise.complete(Status.OK()));
  }

  private HealthCheckHandler getHealthCheckHandler() {
    return HealthCheckHandler.create(vertx, NoAuthRequiredAuthenticationProvider.create())
        .register(
            "db",
            Duration.ofSeconds(5L).toMillis(),
            promise -> {
              log.info("doing db health check");
              new DbPing(pool).check(promise);
            });
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void stop(Promise<Void> stopPromise) {
    System.err.println("stopping");
    MultiCompletePromise multiCompletePromise = MultiCompletePromise.create(stopPromise, 2);

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

    consumer.unregister().onComplete(multiCompletePromise::complete);

    FutureUtil.awaitTermination().onComplete(multiCompletePromise::complete);
  }

  private static class DbPing extends TransactionBoundary {

    private DbPing(Pool pool) {
      super(pool);
    }

    private void check(Promise<Status> promise) {
      doInTransaction(conn -> conn.query("SELECT 1").execute())
          .onFailure(err -> promise.complete(Status.KO()))
          .onSuccess(projection -> promise.complete(Status.OK()));
    }
  }
}
