/* Licensed under Apache-2.0 2023. */
package com.example.iam.rpc.service;

import com.example.iam.rpc.api.IamRpcService;
import dagger.Binds;
import dagger.Module;

@Module
interface ServiceModuleBindings {

  @Binds
  TokenService bindsTokenService(TokenServiceImpl tokenService);

  @Binds
  IamRpcService bindsIamRpcService(IamRpcServiceImpl iamRpcService);
}
