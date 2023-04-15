package com.example.starter.repository;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

import com.example.starter.config.Config;
import com.example.starter.entity.User;
import com.example.starter.web.route.dto.LoginRequestDto;
import com.example.starter.web.route.dto.LoginResponseDto;
import com.example.starter.web.route.dto.RefreshRequestDto;
import com.example.starter.web.route.dto.RefreshResponseDto;
import com.example.starter.web.route.dto.RegisterRequestDto;
import com.example.starter.web.route.dto.RegisterResponseDto;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.impl.types.BulkType;
import io.vertx.redis.client.impl.types.NumberType;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class RedisDB implements UserRepository {

  private final RedisAPI redisAPI;
  private final JWTAuth jwtAuth;
  private final JWTAuth jwtRefresh;

  @Inject
  public RedisDB(Vertx vertx, Config.RedisConfig redisConfig) {
    Redis client = Redis.createClient(vertx, redisConfig.uri());
    redisAPI = RedisAPI.api(client);

    redisAPI
        .ping(List.of(""))
        .onFailure(throwable -> log.log(SEVERE, "failed to ping redis", throwable))
        .onSuccess(resp -> log.info("pinged redis"));

    jwtAuth =
        JWTAuth.create(
            vertx,
            new JWTAuthOptions()
                .setJWTOptions(
                    new JWTOptions().setExpiresInMinutes(1).setIssuer("iam").setSubject("iam"))
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

    jwtRefresh =
        JWTAuth.create(
            vertx,
            new JWTAuthOptions()
                .setJWTOptions(
                    new JWTOptions().setExpiresInMinutes(10).setIssuer("iam").setSubject("iam"))
                .addPubSecKey(
                    new PubSecKeyOptions()
                        .setId("refreshKey1")
                        .setAlgorithm("HS256")
                        .setBuffer("123anothersupersecretkey789")));

    String test = generateToken(jwtAuth, "test");

    jwtAuth
        .authenticate(new TokenCredentials(test))
        .onSuccess(
            user -> {
              // principal contains issuer and everything added in jwtAuth.generateToken below
              log.log(INFO, "user: {0}", new Object[] {user.principal()});
            })
        .onFailure(
            err -> {
              // Failed!
              log.log(SEVERE, "failed", err);
            });
  }

  @Override
  public Future<LoginResponseDto> login(LoginRequestDto requestDto) {
    return redisAPI
        .hget(requestDto.username(), User.PASSWORD_FIELD)
        .map(
            response -> {
              if (!(response instanceof BulkType bt
                  && bt.toString().equals(requestDto.password()))) {
                throw new IllegalArgumentException("user not found");
              }

              String token = authToken(requestDto.username());
              String refreshToken = refreshToken(requestDto.username());

              return new LoginResponseDto(token, refreshToken);
            })
        .flatMap(
            loginResponseDto ->
                redisAPI
                    .hset(
                        List.of(
                            requestDto.username(),
                            User.REFRESH_TOKEN_FIELD,
                            loginResponseDto.refreshToken()))
                    .map(resp -> loginResponseDto)
                    .onComplete(resp -> log.info("user logged in")));
  }

  @Override
  public Future<RefreshResponseDto> refresh(RefreshRequestDto requestDto) {
    return redisAPI
        .hget(requestDto.username(), User.REFRESH_TOKEN_FIELD)
        .map(
            response -> {
              if (!(response instanceof BulkType bt && bt.toString().equals(requestDto.token()))) {
                throw new IllegalArgumentException("invalid token refresh");
              }

              String token = authToken(requestDto.username());
              String refreshToken = refreshToken(requestDto.username());

              return new RefreshResponseDto(token, refreshToken);
            })
        .flatMap(
            loginResponseDto ->
                redisAPI
                    .hset(
                        List.of(
                            requestDto.username(),
                            User.REFRESH_TOKEN_FIELD,
                            loginResponseDto.refreshToken()))
                    .map(resp -> loginResponseDto)
                    .onComplete(resp -> log.info("user refreshed")));
  }

  @Override
  public Future<RegisterResponseDto> register(RegisterRequestDto requestDto) {
    return redisAPI
        .exists(List.of(requestDto.username()))
        .map(
            resp -> {
              if (!(resp instanceof NumberType nt && nt.toLong() == 0L)) {
                throw new IllegalArgumentException("user already exists");
              }

              return refreshToken(requestDto.username());
            })
        .flatMap(
            refreshToken ->
                redisAPI
                    .hset(
                        List.of(
                            requestDto.username(),
                            User.PASSWORD_FIELD,
                            requestDto.password(),
                            User.REFRESH_TOKEN_FIELD,
                            refreshToken))
                    .map(resp -> new RegisterResponseDto())
                    .onComplete(resp -> log.info("user registered")));
  }

  private String authToken(String username) {
    return generateToken(jwtAuth, username);
  }

  private String refreshToken(String username) {
    return generateToken(jwtRefresh, username);
  }

  private String generateToken(JWTAuth jwtAuth, String username) {
    return jwtAuth.generateToken(new JsonObject().put("app-id", "iam").put("sub", username));
  }
}
