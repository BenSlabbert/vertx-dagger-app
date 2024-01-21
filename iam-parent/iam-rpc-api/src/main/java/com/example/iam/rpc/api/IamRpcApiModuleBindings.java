/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

import dagger.Binds;
import dagger.Module;

@Module
interface IamRpcApiModuleBindings {

  @Binds
  AuthenticationIntegration toAuthenticationIntegration(IamIntegration iamIntegration);
}
