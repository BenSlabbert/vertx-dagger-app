/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking.jdbc;

import dagger.Module;

@Module(includes = ModuleBindings.class)
public interface JdbcTransactionManagerModule {

  JdbcTransactionManager jdbcTransactionManager();

  JdbcUtils jdbcUtils();

  JdbcQueryRunner jdbcQueryRunner();
}
