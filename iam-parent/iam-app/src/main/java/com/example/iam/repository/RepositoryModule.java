/* Licensed under Apache-2.0 2023. */
package com.example.iam.repository;

import dagger.Module;

@Module(includes = RepositoryModuleBindings.class)
public interface RepositoryModule {

  UserRepository userRepository();
}
