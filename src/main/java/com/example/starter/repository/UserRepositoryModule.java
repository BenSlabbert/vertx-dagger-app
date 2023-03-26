package com.example.starter.repository;

import dagger.Binds;
import dagger.Module;
import javax.inject.Singleton;

@Module
public interface UserRepositoryModule {

  @Binds
  @Singleton
  UserRepository create(RedisDB redisDB);
}
