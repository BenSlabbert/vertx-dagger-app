/* Licensed under Apache-2.0 2023. */
package com.example.payment.config;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module(includes = {JooqConfig.class, BlockingJdbcPoolConfig.class})
public interface ConfigModule {

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseablePgPoolConfig(BlockingJdbcPoolConfig closeablePool);
}
