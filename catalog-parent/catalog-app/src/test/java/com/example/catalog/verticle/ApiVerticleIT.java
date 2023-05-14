package com.example.catalog.verticle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.example.catalog.TestcontainerLogConsumer;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.FindAllResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.core.json.JsonObject;
import java.time.Duration;
import lombok.extern.java.Log;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Log
@Testcontainers
class ApiVerticleIT {

  @Rule public Network network = Network.newNetwork();

  @Container
  public GenericContainer<?> postgres =
      new GenericContainer<>(DockerImageName.parse("postgres:15-alpine"))
          .withExposedPorts(5432)
          .withNetwork(network)
          .withEnv("POSTGRES_USER", "user")
          .withEnv("POSTGRES_PASSWORD", "password")
          .withEnv("POSTGRES_DB", "db")
          .withNetworkAliases("postgres")
          .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 1))
          .withLogConsumer(new TestcontainerLogConsumer());

  @Container
  public GenericContainer<?> migrator =
      new GenericContainer<>(DockerImageName.parse("catalog-migration:jvm-latest"))
          .withNetwork(network)
          .withNetworkAliases("migrator")
          .dependsOn(postgres)
          .waitingFor(Wait.forLogMessage(".*migration complete.*", 1))
          .withClasspathResourceMapping("it-config.json", "/config.json", BindMode.READ_ONLY)
          .withCommand("/config.json")
          .withLogConsumer(new TestcontainerLogConsumer());

  @Container
  public GenericContainer<?> app =
      new GenericContainer<>(DockerImageName.parse("catalog:native-latest"))
          .withExposedPorts(8080)
          .withNetwork(network)
          .withNetworkAliases("app")
          .dependsOn(postgres, migrator)
          .withEnv("DISABLE_SECURITY", Boolean.TRUE.toString())
          .waitingFor(
              Wait.forLogMessage(".*deployment id.*", 1).withStartupTimeout(Duration.ofSeconds(5L)))
          .withClasspathResourceMapping("it-config.json", "/config.json", BindMode.READ_ONLY)
          .withCommand("/config.json")
          .withLogConsumer(new TestcontainerLogConsumer());

  @BeforeEach
  public void before() {
    RestAssured.baseURI = "http://" + app.getHost();
    RestAssured.port = app.getMappedPort(8080);
    log.info("RestAssured.port: " + RestAssured.port);
  }

  @AfterEach
  public void after() {
    RestAssured.reset();
  }

  @Test
  void fullHappyPath() {
    // find all empty
    String getItemsJsonResponse =
        RestAssured.given()
            .get("/api/items")
            .then()
            .assertThat()
            .statusCode(HttpResponseStatus.OK.code())
            .extract()
            .asString();

    assertThat(new FindAllResponseDto(new JsonObject(getItemsJsonResponse)))
        .isNotNull()
        .extracting(FindAllResponseDto::dtos)
        .satisfies(dtos -> assertThat(dtos).isEmpty());

    // create
    String createItemJsonResponse =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(new CreateItemRequestDto("new_item", 123L).toJson().encode())
            .post("/api/create")
            .then()
            .assertThat()
            .statusCode(HttpResponseStatus.CREATED.code())
            .extract()
            .asString();

    CreateItemResponseDto createItemResponseDto =
        new CreateItemResponseDto(new JsonObject(createItemJsonResponse));
    assertThat(createItemResponseDto)
        .isNotNull()
        .satisfies(
            dto ->
                assertSoftly(
                    softly -> {
                      softly.assertThat(dto.id()).isNotNull();
                      softly.assertThat(dto.name()).isEqualTo("new_item");
                      softly.assertThat(dto.priceInCents()).isEqualTo(123L);
                    }));

    getItemsJsonResponse =
        RestAssured.given()
            .get("/api/items")
            .then()
            .assertThat()
            .statusCode(HttpResponseStatus.OK.code())
            .extract()
            .asString();

    assertThat(new FindAllResponseDto(new JsonObject(getItemsJsonResponse)))
        .isNotNull()
        .extracting(FindAllResponseDto::dtos)
        .satisfies(
            dtos ->
                assertThat(dtos)
                    .singleElement()
                    .satisfies(
                        dto ->
                            assertSoftly(
                                softly -> {
                                  softly.assertThat(dto.id()).isNotNull();
                                  softly.assertThat(dto.name()).isEqualTo("new_item");
                                  softly.assertThat(dto.priceInCents()).isEqualTo(123L);
                                })));

    // find one
    String findOneJsonResponse =
        RestAssured.given()
            .get("/api/" + createItemResponseDto.id())
            .then()
            .assertThat()
            .statusCode(HttpResponseStatus.OK.code())
            .extract()
            .asString();

    assertThat(new FindOneResponseDto(new JsonObject(findOneJsonResponse)))
        .isNotNull()
        .satisfies(
            dto ->
                assertSoftly(
                    softly -> {
                      softly.assertThat(dto.id()).isEqualTo(createItemResponseDto.id());
                      softly.assertThat(dto.name()).isEqualTo("new_item");
                      softly.assertThat(dto.priceInCents()).isEqualTo(123L);
                    }));

    // edit
    RestAssured.given()
        .body(new UpdateItemRequestDto("new_item_updated", 321L).toJson().encode())
        .post("/api/edit/" + createItemResponseDto.id())
        .then()
        .assertThat()
        .statusCode(HttpResponseStatus.NO_CONTENT.code());

    findOneJsonResponse =
        RestAssured.given()
            .get("/api/" + createItemResponseDto.id())
            .then()
            .assertThat()
            .statusCode(HttpResponseStatus.OK.code())
            .extract()
            .asString();

    assertThat(new FindOneResponseDto(new JsonObject(findOneJsonResponse)))
        .isNotNull()
        .satisfies(
            dto ->
                assertSoftly(
                    softly -> {
                      softly.assertThat(dto.id()).isEqualTo(createItemResponseDto.id());
                      softly.assertThat(dto.name()).isEqualTo("new_item_updated");
                      softly.assertThat(dto.priceInCents()).isEqualTo(321L);
                    }));

    // delete
    RestAssured.given()
        .delete("/api/" + createItemResponseDto.id())
        .then()
        .assertThat()
        .statusCode(HttpResponseStatus.NO_CONTENT.code());

    // fetch
    RestAssured.given()
        .get("/api/" + createItemResponseDto.id())
        .then()
        .assertThat()
        .statusCode(HttpResponseStatus.NOT_FOUND.code());

    getItemsJsonResponse =
        RestAssured.given()
            .get("/api/items")
            .then()
            .assertThat()
            .statusCode(HttpResponseStatus.OK.code())
            .extract()
            .asString();

    assertThat(new FindAllResponseDto(new JsonObject(getItemsJsonResponse)))
        .isNotNull()
        .extracting(FindAllResponseDto::dtos)
        .satisfies(dtos -> assertThat(dtos).isEmpty());
  }
}
