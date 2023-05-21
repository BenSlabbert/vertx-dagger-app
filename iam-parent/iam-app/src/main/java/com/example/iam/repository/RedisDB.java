package com.example.iam.repository;

import static java.util.logging.Level.SEVERE;

import com.example.commons.config.Config;
import com.example.commons.redis.RedisConstants;
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
    this.redisAPI = RedisAPI.api(client);

    this.redisAPI
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
        .jsonGet(List.of(prefixId(username), "$." + User.PASSWORD_FIELD))
        .map(
            resp -> {
              if (null == resp) {
                throw new HttpException(HttpResponseStatus.BAD_REQUEST.code());
              }

              String passwordFromDB = resp.toString();

              if (null == passwordFromDB) {
                throw new HttpException(HttpResponseStatus.NOT_FOUND.code());
              }

              passwordFromDB = passwordFromDB.substring(2, passwordFromDB.length() - 2);

              if (!password.equals(passwordFromDB)) {
                throw new HttpException(HttpResponseStatus.NOT_FOUND.code());
              }

              return new LoginResponseDto(token, refreshToken);
            })
        .compose(dto -> updateRefreshToken(username, refreshToken, dto))
        .onSuccess(resp -> log.info("user logged in"));
  }

  @Override
  public Future<RefreshResponseDto> refresh(
      String username, String oldRefreshToken, String newToken, String newRefreshToken) {

    return redisAPI
        .jsonGet(List.of(prefixId(username), "$." + User.REFRESH_TOKEN_FIELD))
        .map(
            resp -> {
              if (null == resp) {
                throw new HttpException(HttpResponseStatus.BAD_REQUEST.code());
              }

              String refreshTokenFromDb = resp.toString();
              refreshTokenFromDb = refreshTokenFromDb.substring(2, refreshTokenFromDb.length() - 2);

              if (!oldRefreshToken.equals(refreshTokenFromDb)) {
                throw new HttpException(HttpResponseStatus.NOT_FOUND.code());
              }

              return new RefreshResponseDto(newToken, newRefreshToken);
            })
        .compose(dto -> updateRefreshToken(username, newRefreshToken, dto))
        .onSuccess(resp -> log.info("user refreshed"));
  }

  @Override
  public Future<RegisterResponseDto> register(
      String username, String password, String token, String refreshToken) {

    return redisAPI
        .jsonSet(
            List.of(
                prefixId(username),
                RedisConstants.DOCUMENT_ROOT,
                new User(username, password, refreshToken).toJson().encode(),
                RedisConstants.SET_IF_DOES_NOT_EXIST))
        .map(
            resp -> {
              if (null == resp) {
                // value could not be set
                throw new HttpException(HttpResponseStatus.CONFLICT.code());
              }

              return new RegisterResponseDto();
            })
        .onSuccess(resp -> log.info("user registered"));
  }

  private <T> Future<T> updateRefreshToken(String username, String newRefreshToken, T echo) {
    return redisAPI
        .jsonSet(
            List.of(
                prefixId(username),
                "$." + User.REFRESH_TOKEN_FIELD,
                // must quote values back to redis
                "\"" + newRefreshToken + "\"",
                RedisConstants.SET_IF_EXIST))
        .map(resp -> echo);
  }

  private static String prefixId(String username) {
    return "user:" + username;
  }
}
