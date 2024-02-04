/* Licensed under Apache-2.0 2024. */
package com.example.starter.warehouse.client;

import dagger.Module;

@Module
public interface WarehouseClientModule {

  WarehouseClientFactory warehouseClientFactory();
}
