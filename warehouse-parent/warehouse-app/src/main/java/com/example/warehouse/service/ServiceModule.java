/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.service;

import dagger.Module;

@Module(includes = ServiceModuleBindings.class)
public interface ServiceModule {

  ServiceLifecycleManagement serviceLifecycleManagement();
}
