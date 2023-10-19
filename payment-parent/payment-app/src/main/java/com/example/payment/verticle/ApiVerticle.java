/* Licensed under Apache-2.0 2023. */
package com.example.payment.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
public class ApiVerticle extends AbstractVerticle {

  @Inject
  public ApiVerticle() {}

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
