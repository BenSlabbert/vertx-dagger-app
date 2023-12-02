/* Licensed under Apache-2.0 2023. */
package com.example.iam.verticle;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.commons.TestcontainerLogConsumer;
import com.example.iam.web.route.dto.LoginRequestDto;
import com.example.iam.web.route.dto.LoginResponseDto;
import com.example.iam.web.route.dto.RefreshRequestDto;
import com.example.iam.web.route.dto.RefreshResponseDto;
import com.example.iam.web.route.dto.RegisterRequestDto;
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
  public GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis/redis-stack-server:latest"))
          .withExposedPorts(6379)
          .withNetwork(network)
          .withNetworkAliases("redis")
          .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1))
          .withLogConsumer(new TestcontainerLogConsumer("redis"));

  @Container
  public GenericContainer<?> app =
      new GenericContainer<>(
              DockerImageName.parse("iam:" + System.getProperty("testImageTag", "jvm") + "-latest"))
          .withExposedPorts(8080)
          .withNetwork(network)
          .withNetworkAliases("app")
          .dependsOn(redis)
          .waitingFor(
              Wait.forLogMessage(".*deployment id.*", 1).withStartupTimeout(Duration.ofSeconds(5L)))
          .withClasspathResourceMapping("it-config.json", "/config.json", BindMode.READ_ONLY)
          .withCommand("/config.json")
          .withLogConsumer(new TestcontainerLogConsumer("app"));

  @BeforeEach
  void before() {
    RestAssured.baseURI = "http://" + app.getHost();
    RestAssured.port = app.getMappedPort(8080);
  }

  @AfterEach
  void after() {
    RestAssured.reset();
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

    String stringJsonResponse =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(login)
            .post("/api/login")
            .then()
            .assertThat()
            .statusCode(HttpResponseStatus.CREATED.code())
            .extract()
            .asString();

    var loginResponseDto = new LoginResponseDto(new JsonObject(stringJsonResponse));
    assertThat(loginResponseDto.token()).isNotNull();
    assertThat(loginResponseDto.refreshToken()).isNotNull();

    String refresh =
        new RefreshRequestDto("name", loginResponseDto.refreshToken()).toJson().encode();

    stringJsonResponse =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(refresh)
            .post("/api/refresh")
            .then()
            .assertThat()
            .statusCode(HttpResponseStatus.CREATED.code())
            .extract()
            .asString();

    var refreshResponseDto = new RefreshResponseDto(new JsonObject(stringJsonResponse));
    assertThat(refreshResponseDto.token()).isNotNull();
    assertThat(refreshResponseDto.refreshToken()).isNotNull();
  }
}
