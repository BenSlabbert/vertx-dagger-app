/* Licensed under Apache-2.0 2023. */
package com.example.iam.service;

import dagger.Module;
import github.benslabbert.vertxdaggerapp.api.iam.auth.IamAuthApi;

@Module(includes = ServiceModuleBindings.class)
public interface ServiceModule {

  IamAuthApi iamAuthApi();

  TokenService tokenService();
}
