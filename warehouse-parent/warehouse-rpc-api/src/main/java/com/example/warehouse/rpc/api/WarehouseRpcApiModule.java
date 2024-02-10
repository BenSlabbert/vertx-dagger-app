/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.rpc.api;

import dagger.Module;

@Module(includes = {WarehouseRpcServiceProvider.class, WarehouseRpcApiModuleBindings.class})
public interface WarehouseRpcApiModule {

  WarehouseRpcService warehouseRpcService();
}
