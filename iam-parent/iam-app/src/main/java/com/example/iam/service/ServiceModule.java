/* Licensed under Apache-2.0 2023. */
package com.example.iam.service;

import com.example.iam.auth.api.IamAuthApi;
import dagger.Module;

@Module(includes = ServiceModuleBindings.class)
public interface ServiceModule {

  IamAuthApi iamAuthApi();

  TokenService tokenService();

  ServiceLifecycleManagement serviceLifecycleManagement();
}
