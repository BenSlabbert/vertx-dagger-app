package com.example.iam.repository;

import static java.util.logging.Level.SEVERE;

import com.example.commons.config.Config;
import com.example.iam.entity.User;
import com.example.iam.web.route.dto.LoginResponseDto;
import com.example.iam.web.route.dto.RefreshResponseDto;
import com.example.iam.web.route.dto.RegisterResponseDto;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.HttpException;
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
        .onFailure(err -> log.log(SEVERE, "failed to ping redis", err))
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
                throw new HttpException(HttpResponseStatus.NOT_FOUND.code());
              }

              return new LoginResponseDto(token, refreshToken);
            })
        .compose(
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
                throw new HttpException(HttpResponseStatus.BAD_REQUEST.code());
              }

              return new RefreshResponseDto(newToken, newRefreshToken);
            })
        .compose(
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
                throw new HttpException(HttpResponseStatus.CONFLICT.code());
              }

              return null;
            })
        .compose(
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
