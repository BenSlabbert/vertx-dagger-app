/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

import io.vertx.core.Future;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class IamIntegration implements AuthenticationIntegration {

  private final IamRpcService iamRpcService;

  @Inject
  IamIntegration(IamRpcService iamRpcService) {
    this.iamRpcService = iamRpcService;
  }

  @Override
  public Future<CheckTokenResponse> isTokenValid(String token) {
    return iamRpcService.check(CheckTokenRequest.builder().token(token).build());
  }
}
