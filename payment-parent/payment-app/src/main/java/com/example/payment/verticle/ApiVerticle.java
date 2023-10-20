/* Licensed under Apache-2.0 2023. */
package com.example.payment.verticle;

import com.example.payment.service.KafkaService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
public class ApiVerticle extends AbstractVerticle {

  @Inject
  public ApiVerticle(KafkaService kafkaService) {}

  @Override
  public void start(Promise<Void> startPromise) {
    log.info("starting ApiVerticle");
    startPromise.complete();
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    log.warning("stopping");
    stopPromise.complete();
  }
}
