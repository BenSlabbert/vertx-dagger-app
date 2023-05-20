package com.example.catalog.service;

import com.example.catalog.TestcontainerLogConsumer;
import com.example.catalog.entity.Item;
import com.example.commons.config.Config;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.UUID;
import lombok.extern.java.Log;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Log
@Testcontainers
@ExtendWith(VertxExtension.class)
class RedisEmitterIT {

  @Rule public Network network = Network.newNetwork();

  @Container
  public GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis/redis-stack-server:latest"))
          .withExposedPorts(6379)
          .withNetwork(network)
          .withNetworkAliases("redis")
          .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1))
          .withLogConsumer(new TestcontainerLogConsumer());

  @Test
  void test(Vertx vertx, VertxTestContext testContext) {
    Emitter emitter =
        new RedisEmitter(
            vertx, new Config.RedisConfig(redis.getHost(), redis.getMappedPort(6379), 0));

    emitter
        .emit(new Item(UUID.randomUUID(), "name", 123L))
        .onFailure(testContext::failNow)
        .onSuccess(v -> testContext.completeNow());
  }
}
