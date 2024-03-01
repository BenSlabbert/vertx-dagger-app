/* Licensed under Apache-2.0 2024. */
package com.example.client.truck.verticle;

import com.example.client.truck.service.JobService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import javax.inject.Inject;

public class TruckVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(TruckVerticle.class);

  private final JobService jobService;

  private volatile long timerId = 0L;

  @Inject
  TruckVerticle(JobService jobService) {
    this.jobService = jobService;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.exceptionHandler(err -> log.error("unhandled exception", err));
    log.info("starting TruckVerticle");
    timerId = vertx.setPeriodic(1000L, 5000L, jobService);
    startPromise.complete();
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    if (timerId != 0L) {
      boolean cancelled = vertx.cancelTimer(timerId);
      System.err.println("cancelled: " + cancelled);
    }
    stopPromise.complete();
  }
}
