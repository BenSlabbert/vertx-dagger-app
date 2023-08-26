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
