/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.service;

import dagger.Binds;
import dagger.Module;
import github.benslabbert.vertxdaggerapp.api.rpc.warehouse.WarehouseRpcService;

@Module
interface ServiceModuleBindings {

  @Binds
  WarehouseRpcService warehouseRpcService(WarehouseRpcServiceImpl warehouseRpcService);
}
