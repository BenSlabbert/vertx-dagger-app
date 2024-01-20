/* Licensed under Apache-2.0 2023. */
package com.example.iam;

import static com.example.commons.FreePortUtility.getPort;

import com.example.commons.TestcontainerLogConsumer;
import com.example.commons.config.Config;
import com.example.iam.ioc.DaggerTestProvider;
import com.example.iam.ioc.TestProvider;
import com.example.iam.verticle.ApiVerticle;
import io.restassured.RestAssured;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(VertxExtension.class)
public abstract class TestBase {

  protected static final int HTTP_PORT = getPort();

  protected TestProvider provider;

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

  @BeforeAll
  static void beforeAll() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    Config config =
        new Config(
            new Config.HttpConfig(HTTP_PORT),
            new Config.RedisConfig("127.0.0.1", redis.getMappedPort(6379), 0),
            null,
            new Config.VerticleConfig(1));

    provider =
        DaggerTestProvider.builder()
            .providesVertx(vertx)
            .providesConfig(config)
            .providesHttpConfig(config.httpConfig())
            .providesVerticleConfig(config.verticleConfig())
            .providesRedisConfig(config.redisConfig())
            .build();

    JsonObject cfg = config.encode();
    vertx.deployVerticle(
        new ApiVerticle(),
        new DeploymentOptions().setConfig(cfg),
        testContext.succeedingThenComplete());
  }

  @BeforeEach
  void before() {
    RestAssured.baseURI = "http://127.0.0.1";
    RestAssured.port = HTTP_PORT;
  }

  @AfterEach
  void after() {
    RestAssured.reset();
  }
}
