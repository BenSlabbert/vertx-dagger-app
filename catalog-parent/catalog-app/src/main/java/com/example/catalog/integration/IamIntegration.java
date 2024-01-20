/* Licensed under Apache-2.0 2023. */
package com.example.catalog.integration;

import com.example.iam.rpc.api.CheckTokenRequest;
import com.example.iam.rpc.api.CheckTokenResponse;
import com.example.iam.rpc.api.IamRpcService;
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

  public Future<CheckTokenResponse> isTokenValid(String token) {
    return iamRpcService.check(CheckTokenRequest.builder().token(token).build());
  }
}
