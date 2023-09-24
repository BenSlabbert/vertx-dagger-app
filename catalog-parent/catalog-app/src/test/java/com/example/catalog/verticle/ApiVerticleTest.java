/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.catalog.TestcontainerLogConsumer;
import com.example.catalog.integration.AuthenticationIntegration;
import com.example.catalog.ioc.DaggerTestProvider;
import com.example.catalog.ioc.TestProvider;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.commons.config.Config;
import com.example.migration.FlywayProvider;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.Map;
import org.flywaydb.core.Flyway;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ExtendWith(VertxExtension.class)
class ApiVerticleTest {

  private static final int PORT = 40001;

  private ApiVerticle apiVerticle;

  @Rule public Network network = Network.newNetwork();

  @Container
  public GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
          .withExposedPorts(6379)
          .withNetwork(network)
          .withNetworkAliases("redis")
          .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1))
          .withLogConsumer(new TestcontainerLogConsumer());

  @Container
  public GenericContainer<?> postgres =
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

    AuthenticationIntegration authHandler = Mockito.mock(AuthenticationIntegration.class);
    when(authHandler.isTokenValid(anyString())).thenReturn(Future.succeededFuture(true));

    Config config =
        new Config(
            new Config.HttpConfig(PORT),
            new Config.GrpcConfig(50500),
            new Config.RedisConfig("localhost", redis.getMappedPort(6379), 0),
            new Config.PostgresConfig(
                "localhost", postgres.getMappedPort(5432), "postgres", "postgres", "postgres"),
            Map.of(),
            new Config.VerticleConfig(1));

    TestProvider testProvider =
        DaggerTestProvider.builder()
            .authenticationIntegration(authHandler)
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .redisConfig(config.redisConfig())
            .verticleConfig(config.verticleConfig())
            .serviceRegistryConfig(config.serviceRegistryConfig())
            .build();

    apiVerticle = testProvider.provideNewApiVerticle();
    vertx.deployVerticle(apiVerticle, testContext.succeedingThenComplete());
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = PORT;
  }

  @AfterEach
  public void after() {
    RestAssured.reset();
  }

  @Test
  void apiTest() {
    String createResponseJson =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION.toString(), "Bearer fake")
            .body(new CreateItemRequestDto("name", 123L).toJson().encode())
            .post("/api/create")
            .then()
            .assertThat()
            .statusCode(HttpResponseStatus.CREATED.code())
            .extract()
            .asString();

    CreateItemResponseDto createItemResponseDto =
        new CreateItemResponseDto(new JsonObject(createResponseJson));

    String getResponseJson =
        RestAssured.given()
            .header(HttpHeaders.AUTHORIZATION.toString(), "Bearer fake")
            .get("/api/" + createItemResponseDto.id())
            .then()
            .assertThat()
            .statusCode(OK.code())
            .extract()
            .asString();

    FindOneResponseDto findOneResponseDto = new FindOneResponseDto(new JsonObject(getResponseJson));
    assertThat(findOneResponseDto).isNotNull();
  }

  @AfterEach
  @DisplayName("Check that the verticle is still there")
  void lastChecks(Vertx vertx) {
    assertThat(vertx.deploymentIDs()).isNotEmpty().hasSize(1);
  }
}
