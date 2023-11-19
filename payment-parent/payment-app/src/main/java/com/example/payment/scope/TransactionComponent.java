/* Licensed under Apache-2.0 2023. */
package com.example.payment.scope;

import com.example.payment.scope.repo.Repo;
import com.example.payment.scope.repo.RepoModule;
import dagger.Subcomponent;

@TransactionScope
@Subcomponent(modules = {ScopeModule.class, RepoModule.class})
public interface TransactionComponent {

  TransactionManager transactionManager();

  Repo repo();

  @Subcomponent.Builder
  interface Builder {

    TransactionComponent build();
  }
}
