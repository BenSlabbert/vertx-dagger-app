/* Licensed under Apache-2.0 2023. */
package com.example.iam.grpc;

import static com.example.commons.FreePortUtility.getPort;

import com.example.commons.TestcontainerLogConsumer;
import com.example.commons.config.Config;
import com.example.iam.grpc.verticle.GrpcVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(VertxExtension.class)
public abstract class TestBase {

  protected static final int GRPC_PORT = getPort();

  private static final Network network = Network.newNetwork();

  protected static final GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis/redis-stack-server:latest"))
          .withExposedPorts(6379)
          .withNetwork(network)
          .withNetworkAliases("redis")
          .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1))
          .withLogConsumer(new TestcontainerLogConsumer("redis"));

  static {
    redis.start();
  }

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    Config config =
        new Config(
            new Config.HttpConfig(0),
            new Config.GrpcConfig(GRPC_PORT),
            new Config.RedisConfig("127.0.0.1", redis.getMappedPort(6379), 0),
            null,
            Map.of(),
            new Config.VerticleConfig(1));

    JsonObject cfg = config.encode();
    vertx.deployVerticle(
        new GrpcVerticle(),
        new DeploymentOptions().setConfig(cfg),
        testContext.succeedingThenComplete());
  }
}
