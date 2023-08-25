package com.example.reactivetest.config;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface ConfigModule {

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseable(CloseablePool closeablePool);
}
