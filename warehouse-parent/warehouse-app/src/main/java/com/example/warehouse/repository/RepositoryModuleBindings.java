/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.repository;

import dagger.Binds;
import dagger.Module;

@Module
interface RepositoryModuleBindings {

  @Binds
  DeliveryRepository bindDeliveryRepository(DeliveryRepositoryImpl deliveryRepository);
}
