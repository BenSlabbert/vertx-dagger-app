/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.example.commons.auth.NoAuthRequiredAuthenticationProvider;
import com.example.commons.closer.ClosingService;
import com.example.commons.config.Config;
import com.example.commons.future.FutureUtil;
import com.example.commons.future.MultiCompletePromise;
import com.example.jdbc.ioc.Provider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(JdbcVerticle.class);

  private final ClosingService closingService;
  private final DataSource dataSource;
  private final Provider provider;
  private final Config config;

  private HttpServer httpServer;

  @Inject
  JdbcVerticle(
      Provider provider, ClosingService closingService, DataSource dataSource, Config config) {
    this.closingService = closingService;
    this.dataSource = dataSource;
    this.provider = provider;
    this.config = config;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    Config.HttpConfig httpConfig = config.httpConfig();

    this.httpServer =
        vertx
            .createHttpServer(new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
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
  }

  private Router setupRoutes() {
    HealthChecks hc = HealthChecks.create(vertx);
    hc.register(
        "jdbc",
        Duration.ofSeconds(5).toMillis(),
        promise -> {
          try (var c = dataSource.getConnection()) {
            promise.complete(Status.OK());
          } catch (Exception e) {
            promise.fail(e);
          }
        });

    HealthCheckHandler withHealthChecks = HealthCheckHandler.createWithHealthChecks(hc);
    Router mainRouter = Router.router(vertx);
    mainRouter.get("/health").handler(withHealthChecks);
    mainRouter.get("/ping").handler(getPingHandler());

    // all unmatched requests go here
    mainRouter.route("/*").handler(ctx -> ctx.response().setStatusCode(NOT_FOUND.code()).end());
    return mainRouter;
  }

  private HealthCheckHandler getPingHandler() {
    return HealthCheckHandler.create(vertx, NoAuthRequiredAuthenticationProvider.create())
        .register("ping", promise -> promise.complete(Status.OK()));
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

    provider.close();
    var multiCompletePromise = MultiCompletePromise.create(stopPromise, 2);
    httpServer.close(multiCompletePromise::complete);
    FutureUtil.awaitTermination().onComplete(multiCompletePromise::complete);
  }
}
