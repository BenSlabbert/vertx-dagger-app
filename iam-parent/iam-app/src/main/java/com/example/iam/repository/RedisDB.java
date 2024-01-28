/* Licensed under Apache-2.0 2023. */
package com.example.iam.repository;

import static java.util.logging.Level.SEVERE;

import com.example.commons.redis.RedisConstants;
import com.example.iam.entity.User;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.redis.client.RedisAPI;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
class RedisDB implements UserRepository, AutoCloseable {

  private final RedisAPI redisAPI;

  @Inject
  RedisDB(RedisAPI redisAPI) {
    this.redisAPI = redisAPI;
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
  public Future<Void> login(String username, String password, String token, String refreshToken) {

    return redisAPI
        .jsonGet(List.of(prefixId(username), "$." + User.PASSWORD_FIELD))
        .compose(
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

              return Future.<Void>succeededFuture();
            })
        .compose(dto -> updateRefreshToken(username, refreshToken))
        .onSuccess(resp -> log.info("user logged in"));
  }

  @Override
  public Future<Void> refresh(
      String username, String oldRefreshToken, String newToken, String newRefreshToken) {

    return redisAPI
        .jsonGet(List.of(prefixId(username), "$." + User.REFRESH_TOKEN_FIELD))
        .compose(
            resp -> {
              if (null == resp) {
                throw new HttpException(HttpResponseStatus.BAD_REQUEST.code());
              }

              String refreshTokenFromDb = resp.toString();
              refreshTokenFromDb = refreshTokenFromDb.substring(2, refreshTokenFromDb.length() - 2);

              if (!oldRefreshToken.equals(refreshTokenFromDb)) {
                throw new HttpException(HttpResponseStatus.NOT_FOUND.code());
              }

              return Future.<Void>succeededFuture();
            })
        .compose(dto -> updateRefreshToken(username, newRefreshToken))
        .onSuccess(resp -> log.info("user refreshed"));
  }

  @Override
  public Future<Void> register(
      String username, String password, String token, String refreshToken) {

    return redisAPI
        .jsonSet(
            List.of(
                prefixId(username),
                RedisConstants.DOCUMENT_ROOT,
                new User(username, password, refreshToken).toJson().encode(),
                RedisConstants.SET_IF_DOES_NOT_EXIST))
        .compose(
            resp -> {
              if (null == resp) {
                // value could not be set
                throw new HttpException(HttpResponseStatus.CONFLICT.code());
              }

              return Future.<Void>succeededFuture();
            })
        .onSuccess(resp -> log.info("user registered"));
  }

  private Future<Void> updateRefreshToken(String username, String newRefreshToken) {
    return redisAPI
        .jsonSet(
            List.of(
                prefixId(username),
                "$." + User.REFRESH_TOKEN_FIELD,
                // must quote values back to redis
                "\"" + newRefreshToken + "\"",
                RedisConstants.SET_IF_EXIST))
        .mapEmpty();
  }

  private static String prefixId(String username) {
    return "user:" + username;
  }
}
