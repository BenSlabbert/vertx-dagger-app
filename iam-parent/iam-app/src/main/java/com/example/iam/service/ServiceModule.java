/* Licensed under Apache-2.0 2023. */
package com.example.iam.service;

import dagger.Binds;
import dagger.Module;

@Module
public interface ServiceModule {

  @Binds
  UserService createUserService(UserServiceImpl userService);

  @Binds
  TokenService createTokenService(JwtService jwtService);
}
