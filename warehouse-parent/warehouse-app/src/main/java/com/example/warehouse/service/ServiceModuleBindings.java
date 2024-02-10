/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.service;

import com.example.warehouse.rpc.api.WarehouseRpcService;
import dagger.Binds;
import dagger.Module;

@Module
interface ServiceModuleBindings {

  @Binds
  WarehouseRpcService warehouseRpcService(WarehouseRpcServiceImpl warehouseRpcService);
}
