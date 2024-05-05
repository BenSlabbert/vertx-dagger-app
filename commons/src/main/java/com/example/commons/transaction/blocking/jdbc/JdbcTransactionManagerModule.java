/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking.jdbc;

import dagger.Module;

@Module
public interface JdbcTransactionManagerModule {

  JdbcTransactionManager jdbcTransactionManager();

  JdbcUtils jdbcUtils();

  JdbcQueryRunner jdbcQueryRunner();
}
