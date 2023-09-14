/* Licensed under Apache-2.0 2023. */
package com.example.iam.repository;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface RepositoryModule {

  @Binds
  UserRepository create(RedisDB redisDB);

  // https://stackoverflow.com/a/62025382
  @Binds
  @IntoSet
  AutoCloseable asAutoCloseable(RedisDB redisDB);
}
