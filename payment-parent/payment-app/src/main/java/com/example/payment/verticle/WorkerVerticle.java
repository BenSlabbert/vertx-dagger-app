/* Licensed under Apache-2.0 2023. */
package com.example.payment.verticle;

import com.example.commons.config.Config;
import com.example.commons.config.ParseConfig;
import com.example.commons.future.FutureUtil;
import com.example.payment.ioc.DaggerProvider;
import com.example.payment.ioc.Provider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class WorkerVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(WorkerVerticle.class);

  private Provider dagger;

  private void init() {
    Context orCreateContext = vertx.getOrCreateContext();
    log.info("orCreateContext:" + orCreateContext);
    log.info("context:" + context);
    if (!orCreateContext.isWorkerContext()) {
      throw new IllegalStateException("not running in a worker context");
    }

    log.info("WorkerVerticle constructor");
    Config config = ParseConfig.get(config());

    Objects.requireNonNull(config.postgresConfig());
    Objects.requireNonNull(config.httpConfig());
    Objects.requireNonNull(config.verticleConfig());
    Objects.requireNonNull(config.kafkaConfig());
    Objects.requireNonNull(config.serviceRegistryConfig());

    this.dagger =
        DaggerProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .verticleConfig(config.verticleConfig())
            .kafkaConfig(config.kafkaConfig())
            .serviceRegistryConfig(config.serviceRegistryConfig())
            .postgresConfig(config.postgresConfig())
            .build();
    this.dagger.init();
  }

  @Override
  public void start(Promise<Void> startPromise) {
    init();

    log.info("starting WorkerVerticle");
    log.info("starting WorkerVerticle on thread: %s".formatted(Thread.currentThread().getName()));

    boolean workerContext = vertx.getOrCreateContext().isWorkerContext();
    boolean eventLoopContext = vertx.getOrCreateContext().isEventLoopContext();
    log.info("workerContext: %b, eventLoopContext: %b".formatted(workerContext, eventLoopContext));

    checkDbConnection(startPromise);

    Future<Void> checkKafka =
        dagger
            .kafkaConsumerService()
            .init()
            .onFailure(err -> log.error("failed to verify kafka connection", err));

    checkKafka.onFailure(startPromise::fail).onSuccess(ignore -> startPromise.complete());
  }

  private void checkDbConnection(Promise<Void> startPromise) {
    try (var ignore = dagger.dataSource().getConnection()) {
      log.info("connected to database");
    } catch (Exception e) {
      log.error("failed to get DB connection", e);
      startPromise.fail(e);
    }
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void stop(Promise<Void> stopPromise) {
    System.err.println("stopping");

    Set<AutoCloseable> closeables = dagger.providesServiceLifecycleManagement().closeables();
    System.err.printf("closing created resources [%d]...%n", closeables.size());

    AtomicInteger idx = new AtomicInteger(0);
    for (AutoCloseable service : closeables) {
      FutureUtil.run(
          () -> {
            try {
              System.err.printf("closing: [%d/%d]%n", idx.incrementAndGet(), closeables.size());
              service.close();
            } catch (Exception e) {
              System.err.println("unable to close resources: " + e);
            }
          });
    }

    Future.fromCompletionStage(CompletableFuture.supplyAsync(FutureUtil::awaitTermination))
        .onComplete(
            ignore -> {
              if (ignore.failed()) {
                System.err.println("failed to shutdown safely: " + ignore.cause());
              }
              System.err.println("shutdown complete");
              stopPromise.complete();
            });
  }
}