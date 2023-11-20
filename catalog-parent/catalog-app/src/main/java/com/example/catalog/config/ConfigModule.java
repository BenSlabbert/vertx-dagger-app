/* Licensed under Apache-2.0 2023. */
package com.example.catalog.config;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module(includes = {JooqConfig.class, PoolConfig.class, RedisConfig.class})
public interface ConfigModule {

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseablePoolConfig(PoolConfig closeablePool);

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseableRedisConfig(RedisConfig redisConfig);
}
