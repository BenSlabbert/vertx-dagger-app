/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

import com.example.iam.rpc.api.dto.CheckTokenRequestDto;
import com.example.iam.rpc.api.dto.CheckTokenResponseDto;
import io.vertx.core.Future;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class IamRpcIntegrationImpl implements IamRpcIntegration {

  private final IamRpcService iamRpcService;

  @Inject
  IamRpcIntegrationImpl(IamRpcService iamRpcService) {
    this.iamRpcService = iamRpcService;
  }

  @Override
  public Future<CheckTokenResponseDto> isTokenValid(String token) {
    return iamRpcService.check(CheckTokenRequestDto.builder().token(token).build());
  }
}
