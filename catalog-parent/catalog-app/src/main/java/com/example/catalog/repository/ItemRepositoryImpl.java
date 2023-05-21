package com.example.catalog.repository;

import static java.util.logging.Level.SEVERE;

import com.example.catalog.entity.Item;
import com.example.commons.config.Config;
import com.example.commons.redis.RedisConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;
import io.vertx.redis.client.ResponseType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
class ItemRepositoryImpl implements ItemRepository, AutoCloseable {

  private static final String ITEM_SET = "item-set";
  private static final String ITEM_SEQUENCE = "item-sequence";

  private final RedisAPI redisAPI;

  // TODO:
  //  rewrite using redis https://redis.io/docs/manual/transactions/
  //  use watch + multi,exec to prevent competing writes
  //  watch will ensure that nothing in the transaction is executed
  //  is the watched objects are changes
  //  there are no rollbacks
  //  all commands will be executed even if there are failures
  //  It's important to note that even when a command fails, all the other commands in the queue are
  //  processed – Redis will not stop the processing of commands.
  //  maybe use a pattern like:
  //  watch the value
  //  read the value
  //  multi
  //  value update
  //  xadd stream * data updatedValue
  //  exec
  //  this way we will only write to the stream the new value if the watched value is unmodified
  //  and we can remove the emitter

  @Inject
  ItemRepositoryImpl(Vertx vertx, Config.RedisConfig redisConfig) {
    Redis client = Redis.createClient(vertx, redisConfig.uri());
    this.redisAPI = RedisAPI.api(client);

    this.redisAPI
        .ping(List.of(""))
        .onFailure(err -> log.log(SEVERE, "failed to ping redis", err))
        .onSuccess(resp -> log.info("pinged redis"));
  }

  @Override
  public Future<Item> create(String name, long priceInCents) {
    // use a counter as a sequence
    UUID id = UUID.randomUUID();
    Item item = new Item(id, name, priceInCents);
    return redisAPI
        .jsonSet(
            List.of(
                prefixId(id),
                RedisConstants.DOCUMENT_ROOT,
                item.toJson().encode(),
                RedisConstants.SET_IF_DOES_NOT_EXIST))
        .map(
            resp -> {
              if (null == resp) {
                // value could not be set
                throw new HttpException(HttpResponseStatus.CONFLICT.code());
              }

              return item;
            })
        .compose(ignore -> redisAPI.incr(ITEM_SEQUENCE))
        .map(
            incResp -> {
              if (incResp.type() != ResponseType.NUMBER) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              return incResp.toLong();
            })
        .compose(
            sequence ->
                redisAPI.zadd(
                    List.of(
                        ITEM_SET,
                        RedisConstants.SET_IF_DOES_NOT_EXIST,
                        Long.toString(sequence),
                        id.toString())))
        .map(
            res -> {
              if (res.type() != ResponseType.NUMBER) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              if (res.toLong() != 1L) {
                // id already exists in the set
                log.log(Level.WARNING, "item id {0} already exists in the set!", new Object[] {id});
              }

              return item;
            });
  }

  @Override
  public Future<List<Item>> findAll(int from, int to) {
    return redisAPI
        .zrange(List.of(ITEM_SET, Integer.toString(from), Integer.toString(to)))
        .map(
            resp -> {
              if (resp.type() != ResponseType.MULTI) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              return resp.stream().map(id -> UUID.fromString(id.toString())).toList();
            })
        .compose(
            ids -> {
              String join =
                  String.join(" ", ids.stream().map(ItemRepositoryImpl::prefixId).toList());
              return redisAPI.jsonMget(List.of(join, RedisConstants.DOCUMENT_ROOT));
            })
        .map(
            resp -> {
              if (resp.type() != ResponseType.MULTI) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              return resp.stream()
                  .filter(Objects::nonNull)
                  .map(
                      r -> {
                        String string = r.toString();
                        string = string.substring(1, string.length() - 1);
                        return new JsonObject(string);
                      })
                  .map(Item::new)
                  .toList();
            });
  }

  @Override
  public Future<Optional<Item>> findById(UUID id) {
    return redisAPI
        .jsonGet(List.of(prefixId(id), RedisConstants.DOCUMENT_ROOT))
        .map(
            resp -> {
              if (resp == null || resp.type() != ResponseType.BULK) {
                return Optional.empty();
              }

              String string = resp.toString();
              string = string.substring(1, string.length() - 1);
              return Optional.of(new Item(new JsonObject(string)));
            });
  }

  @Override
  public Future<Boolean> update(UUID id, String name, long priceInCents) {
    return redisAPI
        .watch(List.of(prefixId(id)))
        .compose(
            resp -> {
              if (!RedisConstants.OK.equals(resp.toString())) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              return redisAPI.multi();
            })
        .compose(
            resp -> {
              if (!RedisConstants.OK.equals(resp.toString())) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              return redisAPI.jsonSet(
                  List.of(
                      prefixId(id),
                      RedisConstants.DOCUMENT_ROOT,
                      new Item(id, name, priceInCents).toJson().encode(),
                      RedisConstants.SET_IF_EXIST));
            })
        .compose(
            resp -> {
              if (!RedisConstants.QUEUED.equals(resp.toString())) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              return redisAPI.exec();
            })
        .map(
            resp -> {
              // todo: retry if transaction was aborted due to object being changed
              //  or send back an erro to client
              if (resp.type() != ResponseType.MULTI) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              // we only expect one response
              Response next = resp.iterator().next();

              return RedisConstants.OK.equals(next.toString());
            });
  }

  @Override
  public Future<Boolean> delete(UUID id) {
    return redisAPI
        .jsonDel(List.of(prefixId(id), RedisConstants.DOCUMENT_ROOT))
        .map(
            resp -> {
              if (resp.type() != ResponseType.NUMBER) {
                return Boolean.FALSE;
              }

              return resp.toLong() == 1L;
            });
  }

  private static String prefixId(UUID id) {
    return "item:" + id;
  }

  @Override
  public void close() {
    redisAPI.close();
  }
}
