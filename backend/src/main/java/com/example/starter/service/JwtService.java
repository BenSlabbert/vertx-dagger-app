package com.example.starter.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JwtService implements TokenService {

  private final JWTAuth jwtAuth;
  private final JWTAuth jwtRefresh;

  @Inject
  public JwtService(Vertx vertx) {
    this.jwtAuth =
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

    this.jwtRefresh =
        JWTAuth.create(
            vertx,
            new JWTAuthOptions()
                .addPubSecKey(
                    new PubSecKeyOptions()
                        .setId("refreshKey1")
                        .setAlgorithm("HS256")
                        .setBuffer("123anothersupersecretkey789")));
  }

  @Override
  public Future<User> isValidToken(String token) {
    return jwtAuth.authenticate(new TokenCredentials(token));
  }

  @Override
  public String authToken(String username) {
    return generateToken(jwtAuth, username);
  }

  @Override
  public String refreshToken(String username) {
    return generateToken(jwtRefresh, username);
  }

  private String generateToken(JWTAuth jwtAuth, String username) {
    return jwtAuth.generateToken(
        new JsonObject().put("additional-props", "new-prop"),
        new JWTOptions().setExpiresInMinutes(1).setIssuer("iam").setSubject(username));
  }
}
