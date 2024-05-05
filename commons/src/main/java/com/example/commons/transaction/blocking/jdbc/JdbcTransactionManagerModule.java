package com.example.commons.transaction.blocking.jdbc;

import dagger.Module;

@Module(includes = JdbcTransactionManagerProvider.class)
public interface JdbcTransactionManagerModule {}
