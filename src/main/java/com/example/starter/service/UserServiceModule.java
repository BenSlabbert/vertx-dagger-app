package com.example.starter.service;

import dagger.Binds;
import dagger.Module;
import javax.inject.Singleton;

@Module
public interface UserServiceModule {

  @Binds
  @Singleton
  UserService create(UserServiceImpl userService);
}
