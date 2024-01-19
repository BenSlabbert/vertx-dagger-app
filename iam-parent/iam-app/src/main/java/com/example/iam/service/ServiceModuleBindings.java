/* Licensed under Apache-2.0 2024. */
package com.example.iam.service;

import dagger.Binds;
import dagger.Module;

@Module
interface ServiceModuleBindings {

  @Binds
  UserService createUserService(UserServiceImpl userService);

  @Binds
  TokenService createTokenService(JwtService jwtService);
}
