/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

import dagger.Module;

@Module(includes = {IamRpcServiceProvider.class, IamRpcApiModuleBindings.class})
public interface IamRpcApiModule {

  IamRpcService iamRpcService();

  IamRpcServiceAuthenticationProvider iamRpcServiceAuthenticationProvider();
}
