/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

import dagger.Binds;
import dagger.Module;
import io.vertx.ext.auth.authentication.AuthenticationProvider;

@Module
interface IamRpcApiModuleBindings {

  @Binds
  AuthenticationProvider iamRpcServiceAuthenticationProvider(
      IamRpcServiceAuthenticationProvider provider);
}
