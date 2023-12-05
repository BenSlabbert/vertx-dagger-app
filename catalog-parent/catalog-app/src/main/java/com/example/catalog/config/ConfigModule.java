/* Licensed under Apache-2.0 2023. */
package com.example.catalog.config;

import com.example.commons.pool.PoolModule;
import com.example.commons.redis.RedisModule;
import dagger.Module;

@Module(includes = {JooqConfig.class, PoolModule.class, RedisModule.class})
public interface ConfigModule {}
