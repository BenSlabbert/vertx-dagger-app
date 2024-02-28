/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

import dagger.Module;
import io.vertx.ext.auth.authentication.AuthenticationProvider;

@Module(includes = {IamRpcServiceProvider.class, IamRpcApiModuleBindings.class})
public interface IamRpcApiModule {

  IamRpcService iamRpcService();

  AuthenticationProvider iamRpcServiceAuthenticationProvider();
}
