/* Licensed under Apache-2.0 2024. */
package com.example.iam.service;

import com.example.iam.auth.api.IamAuthApi;
import dagger.Binds;
import dagger.Module;

@Module
interface ServiceModuleBindings {

  @Binds
  IamAuthApi createUserService(IamAuthApiImpl iamAuthApi);

  @Binds
  TokenService createTokenService(JwtService jwtService);
}
