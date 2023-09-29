/* Licensed under Apache-2.0 2023. */
package com.example.catalog.repository;

import static com.example.commons.redis.RedisConstants.FUZZY;
import static com.example.commons.redis.RedisConstants.MAX;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;
import io.vertx.redis.client.ResponseType;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
class RedisSuggestionService implements SuggestionService, AutoCloseable {

  private static final String ITEM_SUGGESTION_DICTIONARY = "item-suggestions";

  private final RedisAPI redisAPI;

  @Inject
  RedisSuggestionService(RedisAPI redisAPI) {
    this.redisAPI = redisAPI;
  }

  @Override
  public Future<Void> create(String name) {
    return redisAPI
        .incr(name)
        .compose(
            resp -> {
              if (resp.type() != ResponseType.NUMBER) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              if (resp.toLong() == 1L) {
                redisAPI
                    .ftSugadd(List.of(ITEM_SUGGESTION_DICTIONARY, name, Integer.toString(1)))
                    .onSuccess(ignore -> log.info("added " + name + " to suggestion dictionary"));
              }

              return Future.succeededFuture();
            });
  }

  @Override
  public Future<List<String>> suggest(String name) {
    return redisAPI
        .ftSugget(List.of(ITEM_SUGGESTION_DICTIONARY, name, FUZZY, MAX, Integer.toString(5)))
        .map(
            resp -> {
              if (null == resp) {
                return List.of();
              }

              if (resp.type() != ResponseType.MULTI) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              return resp.stream().map(Response::toString).toList();
            });
  }

  @Override
  public Future<Void> update(String oldName, String newName) {
    return redisAPI
        .decr(oldName)
        .compose(
            resp -> {
              if (resp.type() != ResponseType.NUMBER) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              if (resp.toLong() == 0L) {
                redisAPI
                    .ftSugdel(List.of(ITEM_SUGGESTION_DICTIONARY, oldName))
                    .onSuccess(
                        ignore -> log.info("deleted " + oldName + " from suggestion dictionary"));
              }

              return create(newName);
            });
  }

  @Override
  public Future<Void> delete(String name) {
    return redisAPI
        .decr(name)
        .compose(
            resp -> {
              if (resp.type() != ResponseType.NUMBER) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              if (resp.toLong() == 0L) {
                redisAPI
                    .ftSugdel(List.of(ITEM_SUGGESTION_DICTIONARY, name))
                    .onSuccess(
                        ignore -> log.info("deleted " + name + " from suggestion dictionary"));
              }

              return null;
            });
  }

  @Override
  public void close() {
    redisAPI.close();
  }
}
