package com.example.commons.transaction.blocking.jdbc;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Module
class JdbcTransactionManagerProvider {

  private JdbcTransactionManagerProvider() {}

  @Provides
  @Singleton
  static JdbcTransactionManager jdbcTransactionManager(DataSource dataSource) {
    return new JdbcTransactionManager(dataSource);
  }
}
