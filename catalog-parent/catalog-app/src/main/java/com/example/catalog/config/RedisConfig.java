/* Licensed under Apache-2.0 2023. */
package com.example.catalog.config;

import com.example.commons.config.Config;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import javax.inject.Inject;

@Module
class RedisConfig implements AutoCloseable {

  @Inject
  RedisConfig() {}

  private static RedisAPI redisAPI = null;

  @Provides
  static synchronized RedisAPI providesRedisAPI(Vertx vertx, Config.RedisConfig redisConfig) {
    if (redisAPI != null) return redisAPI;

    Redis client = Redis.createClient(vertx, redisConfig.uri());
    redisAPI = RedisAPI.api(client);
    return redisAPI;
  }

  @Override
  public void close() {
    if (null == redisAPI) return;

    redisAPI.close();
  }
}
