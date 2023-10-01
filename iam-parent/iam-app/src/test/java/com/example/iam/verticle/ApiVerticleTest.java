/* Licensed under Apache-2.0 2023. */
package com.example.iam.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.vertx.core.http.HttpMethod.POST;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.iam.TestBase;
import com.example.iam.web.route.dto.LoginRequestDto;
import com.example.iam.web.route.dto.RefreshRequestDto;
import com.example.iam.web.route.dto.RegisterRequestDto;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import java.util.stream.Stream;
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
}
