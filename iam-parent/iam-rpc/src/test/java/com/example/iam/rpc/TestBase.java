/* Licensed under Apache-2.0 2023. */
package com.example.iam.rpc;

import com.example.commons.ConfigEncoder;
import com.example.commons.config.Config;
import com.example.commons.config.Config.RedisConfig;
import com.example.commons.config.Config.VerticleConfig;
import com.example.iam.rpc.verticle.RpcVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public abstract class TestBase {

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    Config config =
        Config.builder()
            .redisConfig(RedisConfig.builder().host("127.0.0.1").port(6379).database(0).build())
            .verticleConfig(VerticleConfig.builder().numberOfInstances(1).build())
            .build();

    JsonObject cfg = ConfigEncoder.encode(config);
    vertx.deployVerticle(
        new RpcVerticle(),
        new DeploymentOptions().setConfig(cfg),
        testContext.succeedingThenComplete());
  }
}
