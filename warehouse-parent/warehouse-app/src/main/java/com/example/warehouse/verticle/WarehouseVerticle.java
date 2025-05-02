/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import github.benslabbert.vertxdaggerapp.api.rpc.warehouse.WarehouseRpcService;
import github.benslabbert.vertxdaggerapp.api.rpc.warehouse.WarehouseRpcServiceVertxEBProxyHandler;
import github.benslabbert.vertxdaggerapp.api.rpc.warehouse.WarehouseRpcService_SecuredActions;
import github.benslabbert.vertxdaggercodegen.commons.security.rpc.SecuredAction;
import github.benslabbert.vertxdaggercodegen.commons.security.rpc.SecuredUnion;
import github.benslabbert.vertxdaggercommons.closer.ClosingService;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.future.FutureUtil;
import github.benslabbert.vertxdaggercommons.future.MultiCompletePromise;
import github.benslabbert.vertxdaggercommons.rpc.UserAccessLoggerInterceptor;
import github.benslabbert.vertxdaggercommons.security.rpc.RpcServiceProxySecurityInterceptor;
import github.benslabbert.vertxdaggercommons.transaction.reactive.TransactionBoundary;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.serviceproxy.AuthenticationInterceptor;
import io.vertx.serviceproxy.ServiceInterceptor;
import io.vertx.serviceproxy.impl.InterceptorHolder;
import io.vertx.sqlclient.Pool;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    InterceptorHolder accessLogger = new InterceptorHolder(UserAccessLoggerInterceptor.create());

    Map<String, SecuredAction> securedActionMap =
        WarehouseRpcService_SecuredActions.getSecuredActions().entrySet().stream()
            .filter(e -> SecuredUnion.Kind.SECURED_ACTION == e.getValue().getKind())
            .collect(Collectors.toMap(Map.Entry::getKey, s -> s.getValue().securedAction()));

    ServiceInterceptor serviceInterceptor =
        RpcServiceProxySecurityInterceptor.create(securedActionMap);
    InterceptorHolder interceptorHolder = new InterceptorHolder(serviceInterceptor);

    this.consumer =
        new WarehouseRpcServiceVertxEBProxyHandler(vertx, warehouseRpcService)
            .register(
                vertx,
                WarehouseRpcService.ADDRESS,
                List.of(authenticationInterceptor, accessLogger, interceptorHolder))
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

    // all unmatched requests go here
    mainRouter.route("/*").handler(ctx -> ctx.response().setStatusCode(NOT_FOUND.code()).end());

    Config.HttpConfig httpConfig = config.httpConfig();
    log.info("starting on port: {}", httpConfig.port());
    vertx
        .createHttpServer(new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
        .requestHandler(mainRouter)
        .listen()
        .onComplete(
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
