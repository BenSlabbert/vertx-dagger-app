/* Licensed under Apache-2.0 2023. */
package com.example.iam.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.vertx.core.http.HttpMethod.POST;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.iam.TestBase;
import com.example.iam.web.route.dto.LoginRequestDto;
import com.example.iam.web.route.dto.LoginResponseDto;
import com.example.iam.web.route.dto.RefreshRequestDto;
import com.example.iam.web.route.dto.RefreshResponseDto;
import com.example.iam.web.route.dto.RegisterRequestDto;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ApiVerticleTest extends TestBase {

  static Stream<Arguments> loginInvalidRequestSource() {
    return Stream.of(
        Arguments.of(new LoginRequestDto("", "")),
        Arguments.of(new LoginRequestDto("1", "")),
        Arguments.of(new LoginRequestDto("", "1")));
  }

  @ParameterizedTest
  @MethodSource("loginInvalidRequestSource")
  void loginInvalidRequest(LoginRequestDto dto, Vertx vertx, VertxTestContext testContext) {
    vertx
        .createHttpClient()
        .request(POST, HTTP_PORT, "127.0.0.1", "/api/login")
        .compose(req -> req.send(dto.toJson().toBuffer()))
        .onComplete(
            testContext.succeeding(
                clientResponse ->
                    testContext.verify(
                        () -> {
                          assertThat(clientResponse.statusCode()).isEqualTo(BAD_REQUEST.code());
                          testContext.completeNow();
                        })));
  }

  static Stream<Arguments> refreshInvalidRequestSource() {
    return Stream.of(
        Arguments.of(new RefreshRequestDto("", "")),
        Arguments.of(new RefreshRequestDto("1", "")),
        Arguments.of(new RefreshRequestDto("", "1")));
  }

  @ParameterizedTest
  @MethodSource("refreshInvalidRequestSource")
  void refreshInvalidRequest(RefreshRequestDto dto, Vertx vertx, VertxTestContext testContext) {
    vertx
        .createHttpClient()
        .request(POST, HTTP_PORT, "127.0.0.1", "/api/refresh")
        .compose(req -> req.send(dto.toJson().toBuffer()))
        .onComplete(
            testContext.succeeding(
                clientResponse ->
                    testContext.verify(
                        () -> {
                          assertThat(clientResponse.statusCode()).isEqualTo(BAD_REQUEST.code());
                          testContext.completeNow();
                        })));
  }

  static Stream<Arguments> registerInvalidRequestSource() {
    return Stream.of(
        Arguments.of(new RegisterRequestDto("", "")),
        Arguments.of(new RegisterRequestDto("1", "")),
        Arguments.of(new RegisterRequestDto("", "1")));
  }

  @ParameterizedTest
  @MethodSource("registerInvalidRequestSource")
  void registerInvalidRequest(RegisterRequestDto dto, Vertx vertx, VertxTestContext testContext) {
    vertx
        .createHttpClient()
        .request(POST, HTTP_PORT, "127.0.0.1", "/api/register")
        .compose(req -> req.send(dto.toJson().toBuffer()))
        .onComplete(
            testContext.succeeding(
                clientResponse ->
                    testContext.verify(
                        () -> {
                          assertThat(clientResponse.statusCode()).isEqualTo(BAD_REQUEST.code());
                          testContext.completeNow();
                        })));
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
