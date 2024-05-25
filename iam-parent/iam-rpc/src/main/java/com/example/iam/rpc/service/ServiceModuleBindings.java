/* Licensed under Apache-2.0 2023. */
package com.example.iam.rpc.service;

import dagger.Binds;
import dagger.Module;
import github.benslabbert.vertxdaggerapp.api.rpc.iam.IamRpcService;

@Module
interface ServiceModuleBindings {

  @Binds
  TokenService bindsTokenService(TokenServiceImpl tokenService);

  @Binds
  IamRpcService bindsIamRpcService(IamRpcServiceImpl iamRpcService);
}
