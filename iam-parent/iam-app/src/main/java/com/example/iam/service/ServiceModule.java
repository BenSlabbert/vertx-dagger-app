/* Licensed under Apache-2.0 2023. */
package com.example.iam.service;

import dagger.Module;

@Module(includes = ServiceModuleBindings.class)
public interface ServiceModule {

  UserService userService();

  TokenService tokenService();

  ServiceLifecycleManagement serviceLifecycleManagement();
}
