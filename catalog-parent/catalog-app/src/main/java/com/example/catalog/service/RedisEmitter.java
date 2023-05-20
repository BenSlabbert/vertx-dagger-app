package com.example.catalog.service;

import static java.util.logging.Level.SEVERE;

import com.example.catalog.entity.Item;
import com.example.commons.config.Config;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.impl.types.BulkType;
import java.util.List;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class RedisEmitter implements Emitter, AutoCloseable {

  private static final String CATALOG_STREAM = "catalog-stream";

  private final RedisAPI redisAPI;

  @Inject
  public RedisEmitter(Vertx vertx, Config.RedisConfig redisConfig) {
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
  public Future<Void> emit(Item item) {
    return redisAPI
        .xadd(List.of(CATALOG_STREAM, "*", "item", item.toJson().encode()))
        .onFailure(err -> log.log(SEVERE, "failed to add to stream", err))
        .map(
            resp -> {
              if (!(resp instanceof BulkType bt)) {
                throw new VertxException("unable to handle stream add response", true);
              }

              String generatedId = bt.toString();
              log.log(Level.INFO, "added value to stream: {0}", new Object[] {generatedId});
              return null;
            });
  }
}
