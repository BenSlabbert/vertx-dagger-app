/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.service;

import com.example.iam.rpc.api.CheckTokenRequest;
import com.example.iam.rpc.api.CheckTokenResponse;
import com.example.iam.rpc.api.IamRpcService;
import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
public class IamRpcServiceImpl implements IamRpcService {

  private static final Logger log = LoggerFactory.getLogger(IamRpcServiceImpl.class);

  private final TokenService tokenService;

  @Override
  public Future<CheckTokenResponse> check(CheckTokenRequest request) {
    log.info("check token is valid");

    return tokenService
        .isValidToken(request.getToken())
        .map(
            user ->
                CheckTokenResponse.builder()
                    .userPrincipal(user.principal().encode())
                    .userAttributes(user.attributes().encode())
                    .valid(true)
                    .build());
  }
}
