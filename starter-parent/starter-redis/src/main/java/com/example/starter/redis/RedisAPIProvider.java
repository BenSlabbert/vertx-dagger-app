/* Licensed under Apache-2.0 2023. */
package com.example.starter.redis;

import com.example.commons.config.Config;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Module
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
class RedisAPIProvider implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(RedisAPIProvider.class);

  private static RedisAPI redisAPI = null;

  @Provides
  @Singleton
  static RedisAPI providesRedisAPI(Vertx vertx, Config config) {
    log.info("creating redis api client");
    Config.RedisConfig redisConfig = config.redisConfig();
    Objects.requireNonNull(redisConfig);

    Redis client = Redis.createClient(vertx, redisConfig.uri());
    redisAPI = RedisAPI.api(client);
    return redisAPI;
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void close() {
    System.err.println("closing redis client");

    if (null == redisAPI) return;

    redisAPI.close();
  }
}
