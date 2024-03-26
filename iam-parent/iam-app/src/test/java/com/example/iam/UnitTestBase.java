/* Licensed under Apache-2.0 2024. */
package com.example.iam;

import com.example.commons.config.Config;
import com.example.iam.ioc.DaggerProvider;
import com.example.iam.ioc.Provider;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public abstract class UnitTestBase {

  protected Provider provider;

  @BeforeEach
  void prepare(Vertx vertx) {
    Config config =
        Config.builder()
            .httpConfig(Config.HttpConfig.builder().port(8080).build())
            .redisConfig(
                Config.RedisConfig.builder().host("127.0.0.1").port(6379).database(0).build())
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
