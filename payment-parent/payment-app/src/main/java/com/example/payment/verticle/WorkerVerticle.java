/* Licensed under Apache-2.0 2023. */
package com.example.payment.verticle;

import com.example.commons.config.Config;
import com.example.commons.config.ParseConfig;
import com.example.commons.future.FutureUtil;
import com.example.commons.mesage.Consumer;
import com.example.payment.ioc.DaggerProvider;
import com.example.payment.ioc.Provider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class WorkerVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(WorkerVerticle.class);

  private Provider dagger;
  private Set<Consumer> consumers = Set.of();

  private void init() {
    Context orCreateContext = vertx.getOrCreateContext();
    log.info("orCreateContext:" + orCreateContext);
    log.info("context:" + context);
    if (!orCreateContext.isWorkerContext()) {
      throw new IllegalStateException("not running in a worker context");
    }

    log.info("WorkerVerticle constructor");
    JsonObject cfg = config();
    Config config = ParseConfig.get(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(config);
    Objects.requireNonNull(config.postgresConfig());
    Objects.requireNonNull(config.httpConfig());
    Objects.requireNonNull(config.verticleConfig());
    Objects.requireNonNull(config.serviceRegistryConfig());

    this.dagger =
        DaggerProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .verticleConfig(config.verticleConfig())
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
    consumers = dagger.consumers();
    consumers.forEach(Consumer::register);
    startPromise.complete();
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
    MultiCompletePromise multiCompletePromise = new MultiCompletePromise(stopPromise, 2);
    System.err.println("stopping");

    Set<AutoCloseable> closeables = dagger.providesServiceLifecycleManagement().closeables();
    System.err.printf("closing created resources [%d]...%n", closeables.size());

    Future.all(consumers.stream().map(Consumer::unregister).toList())
        .onComplete(
            ar -> {
              System.err.println("all eventbus consumers unregistered");
              multiCompletePromise.complete();
            });

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
              multiCompletePromise.complete();
            });
  }

  static class MultiCompletePromise {

    private final Promise<Void> promise;
    private final AtomicInteger counter;

    MultiCompletePromise(Promise<Void> promise, int times) {
      this.promise = promise;
      this.counter = new AtomicInteger(times);
    }

    void complete() {
      if (counter.decrementAndGet() == 0) {
        promise.complete();
      }
    }
  }
}
