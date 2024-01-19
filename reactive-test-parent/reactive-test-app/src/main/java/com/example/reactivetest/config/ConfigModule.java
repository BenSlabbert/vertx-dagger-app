/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.config;

import com.example.commons.pool.PoolModule;
import dagger.Module;

@Module(includes = {JooqConfig.class, PoolModule.class})
public interface ConfigModule {}
