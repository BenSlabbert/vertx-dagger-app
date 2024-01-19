/* Licensed under Apache-2.0 2024. */
package com.example.iam.repository;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
interface RepositoryModuleBindings {

  @Binds
  UserRepository create(RedisDB redisDB);

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseable(RedisDB redisDB);
}
