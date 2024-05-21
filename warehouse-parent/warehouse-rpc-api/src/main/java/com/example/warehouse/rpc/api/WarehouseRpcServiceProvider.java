/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.rpc.api;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;

public class WarehouseRpcServiceProvider {

  private final Vertx vertx;
  private final DeliveryOptions deliveryOptions;

  @AssistedInject
  WarehouseRpcServiceProvider(Vertx vertx, @Assisted DeliveryOptions deliveryOptions) {
    this.vertx = vertx;
    this.deliveryOptions = deliveryOptions;
  }

  public WarehouseRpcService get() {
    return new WarehouseRpcServiceVertxEBClientProxy(
        vertx, WarehouseRpcService.ADDRESS, deliveryOptions);
  }
}
