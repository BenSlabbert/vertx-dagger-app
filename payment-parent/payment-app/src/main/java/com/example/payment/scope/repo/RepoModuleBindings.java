/* Licensed under Apache-2.0 2024. */
package com.example.payment.scope.repo;

import dagger.Binds;
import dagger.Module;

@Module
interface RepoModuleBindings {

  @Binds
  Repo repo(RepoImpl repoImpl);
}
