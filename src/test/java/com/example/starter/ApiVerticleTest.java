package com.example.starter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.starter.config.Config;
import com.example.starter.route.handler.UserHandler;
import com.example.starter.verticle.ApiVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

@ExtendWith(VertxExtension.class)
class ApiVerticleTest {

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    UserHandler userHandler = Mockito.mock(UserHandler.class);

    vertx.deployVerticle(
        new ApiVerticle(userHandler, new Config.HttpConfig(8080)),
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

  @AfterEach
  @DisplayName("Check that the verticle is still there")
  void lastChecks(Vertx vertx) {
    assertThat(vertx.deploymentIDs()).isNotEmpty().hasSize(1);
  }
}
