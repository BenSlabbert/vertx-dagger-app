/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.service;

import dagger.Module;
import github.benslabbert.vertxdaggerapp.api.rpc.warehouse.WarehouseRpcService;

@Module(includes = ServiceModuleBindings.class)
public interface ServiceModule {

  WarehouseRpcService warehouseRpcService();
}
