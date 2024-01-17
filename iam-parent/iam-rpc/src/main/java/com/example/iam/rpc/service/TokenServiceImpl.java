/* Licensed under Apache-2.0 2023. */
package com.example.iam.rpc.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
final class TokenServiceImpl implements TokenService {

  private final Vertx vertx;
  private JWTAuth jwtAuth;

  @Inject
  void init() {
    jwtAuth =
        JWTAuth.create(
            vertx,
            new JWTAuthOptions()
                .addPubSecKey(
                    new PubSecKeyOptions()
                        .setId("authKey1")
                        .setAlgorithm("HS256")
                        .setBuffer("123supersecretkey789"))
                .addPubSecKey(
                    new PubSecKeyOptions()
                        .setId("authKey2")
                        .setAlgorithm("HS256")
                        .setBuffer("321supersecretkey987")));
  }

  @Override
  public Future<User> isValidToken(String token) {
    return jwtAuth.authenticate(new TokenCredentials(token));
  }
}
