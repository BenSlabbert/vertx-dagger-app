/* Licensed under Apache-2.0 2023. */
package com.example.payment.scope;

import dagger.Binds;
import dagger.Module;

@Module
interface ScopeModuleBindings {

  @Binds
  DslProvider dslProvider(TransactionManager transactionManager);
}
