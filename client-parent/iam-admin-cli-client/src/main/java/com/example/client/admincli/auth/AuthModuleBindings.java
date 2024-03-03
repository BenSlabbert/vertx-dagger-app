/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.auth;

import dagger.Binds;
import dagger.Module;

@Module
interface AuthModuleBindings {

  @Binds
  AuthClientFactory authClientProvider(AuthClientProvider authClientProvider);
}
