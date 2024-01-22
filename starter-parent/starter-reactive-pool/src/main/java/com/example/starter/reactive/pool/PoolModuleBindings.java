/* Licensed under Apache-2.0 2023. */
package com.example.starter.reactive.pool;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
interface PoolModuleBindings {

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseablePoolConfig(PoolConfig poolConfig);
}
