/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.repository;

import dagger.Module;

@Module(includes = RepositoryModuleBindings.class)
public interface RepositoryModule {

  DeliveryRepository deliveryRepository();
}
