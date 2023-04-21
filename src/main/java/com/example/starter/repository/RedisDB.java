package com.example.starter.repository;

import static java.util.logging.Level.SEVERE;

import com.example.starter.config.Config;
import com.example.starter.entity.User;
import com.example.starter.web.route.dto.LoginResponseDto;
import com.example.starter.web.route.dto.RefreshResponseDto;
import com.example.starter.web.route.dto.RegisterResponseDto;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
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
public class RedisDB implements UserRepository, AutoCloseable {

  private final RedisAPI redisAPI;

  @Inject
  public RedisDB(Vertx vertx, Config.RedisConfig redisConfig) {
    Redis client = Redis.createClient(vertx, redisConfig.uri());
    redisAPI = RedisAPI.api(client);

    redisAPI
        .ping(List.of(""))
        .onFailure(throwable -> log.log(SEVERE, "failed to ping redis", throwable))
        .onSuccess(resp -> log.info("pinged redis"));
  }

  @Override
  public void close() {
    redisAPI.close();
  }

  @Override
  public Future<LoginResponseDto> login(
      String username, String password, String token, String refreshToken) {
    return redisAPI
        .hget(username, User.PASSWORD_FIELD)
        .map(
            response -> {
              if (!(response instanceof BulkType bt && bt.toString().equals(password))) {
                throw new IllegalArgumentException("user not found");
              }

              return new LoginResponseDto(token, refreshToken);
            })
        .flatMap(
            loginResponseDto ->
                redisAPI
                    .hset(
                        List.of(
                            username, User.REFRESH_TOKEN_FIELD, loginResponseDto.refreshToken()))
                    .map(resp -> loginResponseDto)
                    .onComplete(resp -> log.info("user logged in")));
  }

  @Override
  public Future<RefreshResponseDto> refresh(
      String username, String oldRefreshToken, String newToken, String newRefreshToken) {
    return redisAPI
        .hget(username, User.REFRESH_TOKEN_FIELD)
        .map(
            response -> {
              if (!(response instanceof BulkType bt && bt.toString().equals(oldRefreshToken))) {
                throw new IllegalArgumentException("invalid token refresh");
              }

              return new RefreshResponseDto(newToken, newRefreshToken);
            })
        .flatMap(
            loginResponseDto ->
                redisAPI
                    .hset(
                        List.of(
                            username, User.REFRESH_TOKEN_FIELD, loginResponseDto.refreshToken()))
                    .map(resp -> loginResponseDto)
                    .onComplete(resp -> log.info("user refreshed")));
  }

  @Override
  public Future<RegisterResponseDto> register(
      String username, String password, String token, String refreshToken) {

    return redisAPI
        .exists(List.of(username))
        .map(
            resp -> {
              if (!(resp instanceof NumberType nt && nt.toLong() == 0L)) {
                throw new IllegalArgumentException("user already exists");
              }

              return null;
            })
        .flatMap(
            tokens ->
                redisAPI
                    .hset(
                        List.of(
                            username,
                            User.PASSWORD_FIELD,
                            password,
                            User.REFRESH_TOKEN_FIELD,
                            refreshToken))
                    .map(resp -> new RegisterResponseDto())
                    .onComplete(resp -> log.info("user registered")));
  }
}
