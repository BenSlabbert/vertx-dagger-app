/* Licensed under Apache-2.0 2023. */
package com.example.iam;

import static github.benslabbert.vertxdaggercommons.FreePortUtility.getPort;

import com.example.iam.ioc.DaggerProvider;
import com.example.iam.ioc.Provider;
import github.benslabbert.vertxdaggercommons.ConfigEncoder;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.config.Config.HttpConfig;
import github.benslabbert.vertxdaggercommons.config.Config.RedisConfig;
import github.benslabbert.vertxdaggercommons.docker.DockerContainers;
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

@ExtendWith(VertxExtension.class)
public abstract class VerticleTestBase {

  protected static final int HTTP_PORT = getPort();

  protected Provider provider;

  protected static final GenericContainer<?> redis = DockerContainers.REDIS;

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
        Config.builder()
            .httpConfig(HttpConfig.builder().port(HTTP_PORT).build())
            .redisConfig(
                RedisConfig.builder()
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

    JsonObject cfg = ConfigEncoder.encode(config);
    vertx.deployVerticle(
        provider.apiVerticle(),
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
