package com.example.starter;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.starter.config.Config;
import com.example.starter.service.UserService;
import com.example.starter.verticle.ApiVerticle;
import com.example.starter.web.SchemaValidator;
import com.example.starter.web.route.dto.LoginRequestDto;
import com.example.starter.web.route.dto.RefreshRequestDto;
import com.example.starter.web.route.dto.RegisterRequestDto;
import com.example.starter.web.route.handler.PingHandler;
import com.example.starter.web.route.handler.UserHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

@ExtendWith(VertxExtension.class)
class ApiVerticleTest {

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(
        new ApiVerticle(
            new UserHandler(Mockito.mock(UserService.class), Mockito.mock(SchemaValidator.class)),
            new PingHandler(),
            new Config.HttpConfig(8080)),
        testContext.succeedingThenComplete());
  }

  @Test
  void ping(Vertx vertx, VertxTestContext testContext) {
    vertx
        .createHttpClient()
        .request(HttpMethod.GET, 8080, "localhost", "/ping")
        .compose(req -> req.send().compose(HttpClientResponse::body))
        .onComplete(
            testContext.succeeding(
                buffer ->
                    testContext.verify(
                        () -> {
                          assertThat(buffer).hasToString("pong");
                          testContext.completeNow();
                        })));
  }

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
        .request(HttpMethod.POST, 8080, "localhost", "/api/login")
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
        .request(HttpMethod.POST, 8080, "localhost", "/api/refresh")
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
        .request(HttpMethod.POST, 8080, "localhost", "/api/register")
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

  @AfterEach
  @DisplayName("Check that the verticle is still there")
  void lastChecks(Vertx vertx) {
    assertThat(vertx.deploymentIDs()).isNotEmpty().hasSize(1);
  }
}
