/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.config;

import dagger.Module;
import dagger.Provides;
import java.io.PrintStream;
import javax.inject.Named;

@Module
public interface ConfigModule {

  @SuppressWarnings("java:S106") // logger is not available
  @Provides
  @Named("out")
  static PrintStream provideOut() {
    return System.out;
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Provides
  @Named("err")
  static PrintStream provideErr() {
    return System.err;
  }
}
