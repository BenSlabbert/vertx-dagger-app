/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.rpc.api;

import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import javax.inject.Singleton;

@Module
class WarehouseRpcServiceProvider {

  private WarehouseRpcServiceProvider() {}

  @Provides
  @Singleton
  static WarehouseRpcService provideWarehouseRpcService(Vertx vertx) {
    return WarehouseRpcService.createClientProxy(vertx);
  }
}
