/* Licensed under Apache-2.0 2023. */
package com.example.starter.redis;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
interface RedisModuleBindings {

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseableRedisConfig(RedisAPIProvider redisAPIProvider);
}
