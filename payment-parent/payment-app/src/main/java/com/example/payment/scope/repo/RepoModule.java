/* Licensed under Apache-2.0 2023. */
package com.example.payment.scope.repo;

import dagger.Module;

@Module(includes = RepoModuleBindings.class)
public interface RepoModule {

  Repo repo();
}
