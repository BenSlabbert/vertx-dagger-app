/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking;

import dagger.Module;

@Module(includes = {SimpleTransactionProvider.class})
public interface TransactionManagerModule {}
