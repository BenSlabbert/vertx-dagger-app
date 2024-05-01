/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Module
final class SimpleTransactionProvider {

  private SimpleTransactionProvider() {}

  @Provides
  @Singleton
  static SimpleTransactionManager simpleTransactionManager(DataSource dataSource) {
    return new SimpleTransactionManager(dataSource);
  }
}
