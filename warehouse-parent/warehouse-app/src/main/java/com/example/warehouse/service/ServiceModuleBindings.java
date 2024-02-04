/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.service;

import com.example.warehouse.api.WarehouseApi;
import dagger.Binds;
import dagger.Module;

@Module
interface ServiceModuleBindings {

  @Binds
  WarehouseApi warehouseApi(WarehouseApiImpl warehouseApi);
}
