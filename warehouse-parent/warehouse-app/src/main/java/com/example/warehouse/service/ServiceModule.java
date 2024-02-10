/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.service;

import com.example.warehouse.rpc.api.WarehouseRpcService;
import dagger.Module;

@Module(includes = ServiceModuleBindings.class)
public interface ServiceModule {

  ServiceLifecycleManagement serviceLifecycleManagement();

  WarehouseRpcService warehouseRpcService();
}
