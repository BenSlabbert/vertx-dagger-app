/* Licensed under Apache-2.0 2023. */
package com.example.payment.scope;

import dagger.Binds;
import dagger.Module;

@Module
interface ScopeModule {

  // here we can add out own bindings for this specific scope

  @Binds
  DslProvider dslProvider(TransactionManager transactionManager);
}
