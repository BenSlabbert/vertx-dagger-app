/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.auth;

import dagger.Module;

@Module(includes = AuthModuleBindings.class)
public interface AuthModule {

  AuthClientFactory authClientProvider();
}
