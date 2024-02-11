/* Licensed under Apache-2.0 2024. */
package com.example.client.truck.service;

import dagger.Binds;
import dagger.Module;

@Module
interface ServiceModuleBindings {

  @Binds
  JobService bindJobService(JobServiceImpl jobServiceImpl);
}
