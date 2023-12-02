/* Licensed under Apache-2.0 2023. */
package com.example.iam.grpc.service;

import dagger.Binds;
import dagger.Module;

@Module
interface ServiceModuleBindings {

  @Binds
  TokenService bindsTokenService(TokenServiceImpl tokenService);
}
