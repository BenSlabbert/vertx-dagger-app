/* Licensed under Apache-2.0 2024. */
package com.example.iam;

import static com.example.commons.FreePortUtility.getPort;

import com.example.commons.config.Config;
import com.example.commons.docker.DockerContainers;
import com.example.iam.ioc.DaggerProvider;
import com.example.iam.ioc.Provider;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;

@ExtendWith(VertxExtension.class)
public abstract class IntegrationTestBase {

  protected static final int HTTP_PORT = getPort();

  protected Provider provider;

  protected static final GenericContainer<?> redis = DockerContainers.REDIS;

  static {
    redis.start();
  }

  @BeforeEach
  void prepare(Vertx vertx) {
    Config config =
        Config.builder()
            .httpConfig(Config.HttpConfig.builder().port(HTTP_PORT).build())
            .redisConfig(
                Config.RedisConfig.builder()
                    .host("127.0.0.1")
                    .port(redis.getMappedPort(6379))
                    .database(0)
                    .build())
            .build();

    provider =
        DaggerProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .redisConfig(config.redisConfig())
            .build();
  }
}
