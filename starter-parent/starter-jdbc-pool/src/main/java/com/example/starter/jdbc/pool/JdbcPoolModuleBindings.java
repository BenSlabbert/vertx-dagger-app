/* Licensed under Apache-2.0 2024. */
package com.example.starter.jdbc.pool;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
interface JdbcPoolModuleBindings {

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseablePoolConfig(BlockingJdbcPoolConfig closeablePool);
}
