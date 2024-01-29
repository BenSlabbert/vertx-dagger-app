/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class WarehouseVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    startPromise.complete();
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    stopPromise.complete();
  }
}
