package com.example.starter.route.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.starter.route.handler.dto.LoginRequestDto;
import com.example.starter.route.handler.dto.LoginResponseDto;
import com.example.starter.route.handler.dto.RefreshRequestDto;
import com.example.starter.route.handler.dto.RefreshResponseDto;
import com.example.starter.route.handler.dto.RegisterRequestDto;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.core.json.JsonObject;
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
  public GenericContainer redis =
      new GenericContainer(DockerImageName.parse("redis:7-alpine"))
          .withExposedPorts(6379)
          .withNetwork(network)
          .withNetworkAliases("redis")
          .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1));

  @Container
  public GenericContainer app =
      new GenericContainer(
              DockerImageName.parse(
                  "vertx:" + System.getProperty("testImageTag", "jvm") + "-latest"))
          .withExposedPorts(8080)
          .withNetwork(network)
          .withNetworkAliases("app")
          .dependsOn(redis)
          .waitingFor(Wait.forLogMessage(".*deployment id.*", 1))
          .withClasspathResourceMapping("it-config.json", "/config.json", BindMode.READ_ONLY)
          .withCommand("/config.json");

  @BeforeEach
  public void before() {
    RestAssured.baseURI = "http://" + app.getHost();
    RestAssured.port = app.getFirstMappedPort();
    log.info("RestAssured.port: " + RestAssured.port);
  }

  @AfterEach
  public void after() {
    RestAssured.reset();
  }

  @Test
  void pingTest() {
    String body =
        RestAssured.get("/ping")
            .then()
            .assertThat()
            .statusCode(HttpResponseStatus.OK.code())
            .extract()
            .asString();
    assertThat(body).isEqualTo("pong");
  }

  @Test
  void fullHappyPath() {
    String register = new RegisterRequestDto("name", "pswd").toJson().encode();
    String login = new LoginRequestDto("name", "pswd").toJson().encode();

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(register)
        .post("/api/register")
        .then()
        .assertThat()
        .statusCode(HttpResponseStatus.NO_CONTENT.code());

    String response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(login)
            .post("/api/login")
            .then()
            .assertThat()
            .statusCode(HttpResponseStatus.CREATED.code())
            .extract()
            .asString();

    var loginResponseDto = new LoginResponseDto(new JsonObject(response));
    assertThat(loginResponseDto.token()).isNotNull();
    assertThat(loginResponseDto.refreshToken()).isNotNull();

    String refresh =
        new RefreshRequestDto("name", loginResponseDto.refreshToken()).toJson().encode();

    response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(refresh)
            .post("/api/refresh")
            .then()
            .assertThat()
            .statusCode(HttpResponseStatus.CREATED.code())
            .extract()
            .asString();

    var refreshResponseDto = new RefreshResponseDto(new JsonObject(response));
    assertThat(refreshResponseDto.token()).isNotNull();
    assertThat(refreshResponseDto.refreshToken()).isNotNull();
  }
}
