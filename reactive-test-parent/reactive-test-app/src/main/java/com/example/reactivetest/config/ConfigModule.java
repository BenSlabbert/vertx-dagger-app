/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.config;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module(includes = {JooqConfig.class, PgPoolConfig.class})
public interface ConfigModule {

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseable(PgPoolConfig closeablePool);
}
