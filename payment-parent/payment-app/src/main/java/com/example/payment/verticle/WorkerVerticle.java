/* Licensed under Apache-2.0 2023. */
package com.example.payment.verticle;

import com.example.commons.closer.ClosingService;
import com.example.commons.future.FutureUtil;
import com.example.commons.future.MultiCompletePromise;
import com.example.commons.mesage.Consumer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.ThreadingModel;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(WorkerVerticle.class);

  private final ClosingService closingService;
  private final Set<Consumer> consumers;
  private final DataSource dataSource;

  @Inject
  WorkerVerticle(ClosingService closingService, DataSource dataSource, Set<Consumer> consumers) {
    this.closingService = closingService;
    this.dataSource = dataSource;
    this.consumers = consumers;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.exceptionHandler(err -> log.error("unhandled exception", err));

    log.info("starting WorkerVerticle");
    log.info("starting WorkerVerticle on thread: {}", Thread.currentThread().getName());

    ThreadingModel threadingModel = vertx.getOrCreateContext().threadingModel();
    boolean workerContext = vertx.getOrCreateContext().isWorkerContext();
    boolean eventLoopContext = vertx.getOrCreateContext().isEventLoopContext();
    log.info(
        "threadingModel: {}, workerContext: {}, eventLoopContext: {}",
        threadingModel,
        workerContext,
        eventLoopContext);

    checkDbConnection(startPromise);
    consumers.forEach(Consumer::register);
    startPromise.complete();
  }

  private void checkDbConnection(Promise<Void> startPromise) {
    try (var ignore = dataSource.getConnection()) {
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
    MultiCompletePromise multiCompletePromise = MultiCompletePromise.create(stopPromise, 2);

    Set<AutoCloseable> closeables = closingService.closeables();
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
}
