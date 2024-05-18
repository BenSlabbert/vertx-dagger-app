/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking.jdbc;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
interface ModuleBindings {

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseable(JdbcTransactionManager jdbcTransactionManager);
}
