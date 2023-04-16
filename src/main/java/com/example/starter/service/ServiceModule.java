package com.example.starter.service;

import dagger.Binds;
import dagger.Module;

@Module
public interface ServiceModule {

  @Binds
  UserService create(UserServiceImpl userService);
}
