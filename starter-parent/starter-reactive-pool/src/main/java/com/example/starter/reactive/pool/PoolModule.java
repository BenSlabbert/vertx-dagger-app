/* Licensed under Apache-2.0 2023. */
package com.example.starter.reactive.pool;

import dagger.Module;

@Module(includes = {PoolConfig.class, PoolModuleBindings.class})
public interface PoolModule {}
