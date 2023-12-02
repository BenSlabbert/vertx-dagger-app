/* Licensed under Apache-2.0 2023. */
package com.example.iam.grpc.service;

import com.example.iam.grpc.iam.CheckTokenRequest;
import com.example.iam.grpc.iam.CheckTokenResponse;
import com.example.iam.grpc.iam.IamGrpc;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerRequest;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
public class GrpcService {

  private static final Logger log = LoggerFactory.getLogger(GrpcService.class);

  private final TokenService tokenService;
  private final Vertx vertx;

  public GrpcServer getGrpcServer() {
    return GrpcServer.server(vertx).callHandler(IamGrpc.getCheckTokenMethod(), this::checkToken);
  }

  private void checkToken(GrpcServerRequest<CheckTokenRequest, CheckTokenResponse> request) {
    request
        .exceptionHandler(throwable -> setInternalStatusError(request, throwable))
        .handler(
            checkRequest ->
                tokenService
                    .isValidToken(checkRequest.getToken())
                    .onSuccess(
                        user ->
                            request
                                .response()
                                .end(
                                    CheckTokenResponse.newBuilder()
                                        .setUserPrincipal(user.principal().encode())
                                        .setUserAttributes(user.attributes().encode())
                                        .setValid(true)
                                        .build()))
                    .onFailure(
                        err ->
                            request
                                .response()
                                .end(CheckTokenResponse.newBuilder().setValid(false).build())));
  }

  private void setInternalStatusError(GrpcServerRequest<?, ?> request, Throwable throwable) {
    log.error("exception while handling request", throwable);
    request.response().status(GrpcStatus.INTERNAL).end();
  }
}
