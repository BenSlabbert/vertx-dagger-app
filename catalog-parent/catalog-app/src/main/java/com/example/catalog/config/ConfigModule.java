/* Licensed under Apache-2.0 2023. */
package com.example.catalog.config;

import dagger.Module;

@Module(
    includes = {JooqConfig.class, PoolConfig.class, RedisConfig.class, ConfigModuleBindings.class})
public interface ConfigModule {}
