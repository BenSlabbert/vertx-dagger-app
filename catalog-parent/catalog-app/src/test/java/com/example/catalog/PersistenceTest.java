/* Licensed under Apache-2.0 2023. */
package com.example.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.catalog.integration.AuthenticationIntegration;
import com.example.catalog.ioc.DaggerTestPersistenceProvider;
import com.example.catalog.ioc.TestPersistenceProvider;
import com.example.commons.HttpServerTest;
import com.example.commons.TestcontainerLogConsumer;
import com.example.commons.config.Config;
import com.example.migration.FlywayProvider;
import io.restassured.RestAssured;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.Map;
import org.flywaydb.core.Flyway;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ExtendWith(VertxExtension.class)
public abstract class PersistenceTest extends HttpServerTest {

  protected static final int HTTP_PORT = getPort();
  protected static final int GRPC_PORT = getPort();

  @Rule public Network network = Network.newNetwork();

  @Container
  protected GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis/redis-stack-server:latest"))
          .withExposedPorts(6379)
          .withNetwork(network)
          .withNetworkAliases("redis")
          .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1))
          .withLogConsumer(new TestcontainerLogConsumer());

  @Container
  protected GenericContainer<?> postgres =
      new GenericContainer<>(DockerImageName.parse("postgres:15-alpine"))
          .withExposedPorts(5432)
          .withNetwork(network)
          .withNetworkAliases("postgres")
          .withEnv("POSTGRES_USER", "postgres")
          .withEnv("POSTGRES_PASSWORD", "postgres")
          .withEnv("POSTGRES_DB", "postgres")
          .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 1))
          .withLogConsumer(new TestcontainerLogConsumer());

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    Flyway flyway =
        FlywayProvider.get(
            "localhost", postgres.getMappedPort(5432), "postgres", "postgres", "postgres");
    flyway.clean();
    flyway.migrate();

    AuthenticationIntegration authHandler = mock(AuthenticationIntegration.class);
    when(authHandler.isTokenValid(anyString())).thenReturn(Future.succeededFuture(true));

    Config config =
        new Config(
            new Config.HttpConfig(HTTP_PORT),
            new Config.GrpcConfig(GRPC_PORT),
            new Config.RedisConfig("localhost", redis.getMappedPort(6379), 0),
            new Config.PostgresConfig(
                "localhost", postgres.getMappedPort(5432), "postgres", "postgres", "postgres"),
            Map.of(),
            new Config.VerticleConfig(1));

    TestPersistenceProvider provider =
        DaggerTestPersistenceProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .redisConfig(config.redisConfig())
            .verticleConfig(config.verticleConfig())
            .serviceRegistryConfig(config.serviceRegistryConfig())
            .authenticationIntegration(authHandler)
            .build();

    vertx.deployVerticle(provider.provideNewApiVerticle(), testContext.succeedingThenComplete());
  }

  @BeforeEach
  void before() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = HTTP_PORT;
  }

  @AfterEach
  void after() {
    RestAssured.reset();
  }

  @AfterEach
  void lastChecks(Vertx vertx) {
    assertThat(vertx.deploymentIDs()).isNotEmpty().hasSize(1);
  }
}
