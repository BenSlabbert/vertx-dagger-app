/* Licensed under Apache-2.0 2023. */
package com.example.payment.scope;

import dagger.Module;

@Module(subcomponents = TransactionComponent.class)
public interface TransactionModule {

  // do not expose types outside this scope to the parent component
}
