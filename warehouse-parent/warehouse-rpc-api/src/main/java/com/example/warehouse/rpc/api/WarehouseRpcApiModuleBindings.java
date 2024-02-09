/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.rpc.api;

import dagger.Binds;
import dagger.Module;

@Module
interface WarehouseRpcApiModuleBindings {

  @Binds
  WarehouseRpcIntegration bindWarehouseRpcIntegration(
      WarehouseRpcIntegrationImpl warehouseRpcIntegrationImpl);
}
