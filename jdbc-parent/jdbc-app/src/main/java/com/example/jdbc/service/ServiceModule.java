/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.service;

import dagger.Module;

@Module
public interface ServiceModule {

  JdbcService jdbcService();

  TransactionService transactionService();

  NestedTransactionService nestedTransactionService();
}
