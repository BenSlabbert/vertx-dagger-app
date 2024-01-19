/* Licensed under Apache-2.0 2023. */
package com.example.iam.verticle;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.WRITE;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import lombok.extern.java.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@Log
@Disabled("need to fix docker networking issue")
class ApiVerticleIT {

  @TempDir Path tempDir;

  public GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis/redis-stack-server:latest"))
          .withExposedPorts(6379)
          .withNetworkAliases("redis")
          .waitingFor(
              Wait.forLogMessage(".*Ready to accept connections.*", 1)
                  .withStartupTimeout(Duration.ofSeconds(10L)))
          .withLogConsumer(new TestcontainerLogConsumer("redis"));

  public GenericContainer<?> app =
      new GenericContainer<>(
              DockerImageName.parse("iam:" + System.getProperty("testImageTag", "jvm") + "-latest"))
          .withExposedPorts(8080)
          .withNetworkMode("host")
          .withNetworkAliases("app")
          .dependsOn(redis)
          //          .waitingFor(
          //              Wait.forLogMessage(".*Succeeded in deploying verticle.*", 1)
          //                  .withStartupTimeout(Duration.ofSeconds(10L)))
          //          .withClasspathResourceMapping("it-config.json", "/config.json",
          // BindMode.READ_ONLY)
          .withCommand("-conf", "/config.json")
          .withLogConsumer(new TestcontainerLogConsumer("app"));

  @BeforeEach
  void before() throws IOException {
    redis.start();

    Path tempFile = Files.createTempFile(tempDir, "prefix-", "-cfg");

    JsonObject cfg = new JsonObject();
    cfg.put("httpConfig", new JsonObject().put("port", 8080));
    cfg.put(
        "redisConfig",
        new JsonObject()
            .put("host", "127.0.0.1")
            .put("port", redis.getMappedPort(6379))
            .put("database", 0));
    cfg.put("verticleConfig", new JsonObject().put("numberOfInstances", 1));

    String encoded = cfg.encodePrettily();
    Files.writeString(tempFile, encoded, UTF_8, WRITE);

    String string = tempFile.toString();
    app.withFileSystemBind(string, "/config.json", BindMode.READ_ONLY);

    app.start();
    RestAssured.baseURI = "http://" + app.getHost();
    RestAssured.port = app.getMappedPort(8080);
  }

  @AfterEach
  void after() {
    app.stop();
    redis.stop();
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
