/* Licensed under Apache-2.0 2023. */
package com.example.iam.service;

import com.example.iam.entity.ACL;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class JwtService implements TokenService {

  private static final Logger log = LoggerFactory.getLogger(JwtService.class);

  private final JWTAuth jwtAuth;
  private final JWTAuth jwtRefresh;

  @Inject
  JwtService(Vertx vertx) {
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
  public Future<User> isValidRefresh(String token) {
    return jwtRefresh.authenticate(new TokenCredentials(token));
  }

  @Override
  public Future<User> authenticate(String token) {
    return jwtAuth.authenticate(new TokenCredentials(token));
  }

  @Override
  public String authToken(String username, ACL acl) {
    return generateToken(
        jwtAuth, username, Duration.ofSeconds(30L), json -> json.put("acl", acl.toJson()));
  }

  @Override
  public String refreshToken(String username) {
    return generateToken(jwtRefresh, username, Duration.ofHours(1L));
  }

  private String generateToken(JWTAuth jwtAuth, String username, Duration duration) {
    return generateToken(jwtAuth, username, duration, ignore -> {});
  }

  private String generateToken(
      JWTAuth jwtAuth, String username, Duration duration, Consumer<JsonObject> customizer) {
    // default max lifetime in seconds: 24 hours
    int lifetimeSeconds = 60 * 60 * 24;
    if (duration.toSeconds() <= Integer.MAX_VALUE) {
      lifetimeSeconds = (int) duration.toSeconds();
    } else {
      log.warn("given token duration exceeds: Integer.MAX_VALUE, using default");
    }

    JsonObject rootClaim = new JsonObject().put("additional-props", "new-prop");
    customizer.accept(rootClaim);
    return jwtAuth.generateToken(
        rootClaim,
        new JWTOptions()
            .setExpiresInSeconds(lifetimeSeconds)
            .setIssuer("iam")
            .setAudience(List.of("vertx-dagger-app"))
            .setSubject(username));
  }
}
