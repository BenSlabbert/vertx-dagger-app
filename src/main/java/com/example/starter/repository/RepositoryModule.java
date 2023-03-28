package com.example.starter.repository;

import dagger.Binds;
import dagger.Module;
import javax.inject.Singleton;

@Module
public interface RepositoryModule {

  @Binds
  @Singleton
  UserRepository create(RedisDB redisDB);
}
