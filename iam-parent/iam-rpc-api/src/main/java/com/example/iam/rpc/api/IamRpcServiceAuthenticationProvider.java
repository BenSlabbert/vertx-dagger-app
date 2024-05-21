/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;

import com.example.iam.rpc.api.dto.CheckTokenRequestDto;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.serviceproxy.ServiceException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

@Singleton
class IamRpcServiceAuthenticationProvider implements AuthenticationProvider {

  private final IamRpcService iamRpcService;

  @Inject
  IamRpcServiceAuthenticationProvider(IamRpcService iamRpcService) {
    this.iamRpcService = iamRpcService;
  }

  @Override
  public void authenticate(JsonObject token, Handler<AsyncResult<User>> var2) {
    throw new UnsupportedOperationException("deprecated method not implemented");
  }

  @Override
  public Future<User> authenticate(Credentials credentials) {
    // request must have a header called "auth-token" which is a JWT token
    if (null == credentials) {
      return Future.failedFuture("credentials required");
    }

    String jwtToken = credentials.toJson().getString("token");

    if (StringUtils.isBlank(jwtToken)) {
      return Future.failedFuture("JWT credentials required");
    }

    return iamRpcService
        .check(CheckTokenRequestDto.builder().token(jwtToken).build())
        .compose(
            resp -> {
              if (!resp.valid()) {
                AsyncResult<User> ar =
                    ServiceException.fail(UNAUTHORIZED.code(), "invalid credentials");
                return Future.failedFuture(ar.cause());
              }

              JsonObject principal = new JsonObject(resp.userPrincipal());
              JsonObject attributes = new JsonObject(resp.userAttributes());
              return Future.succeededFuture(User.create(principal, attributes));
            });
  }
}
