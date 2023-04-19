package com.example.starter.verticle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.starter.HttpServerTest;
import com.example.starter.config.Config;
import com.example.starter.grpc.echo.CheckSessionRequest;
import com.example.starter.grpc.echo.IamGrpc;
import com.example.starter.grpc.echo.PingRequest;
import com.example.starter.service.UserService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.common.GrpcReadStream;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

@ExtendWith(VertxExtension.class)
class GrpcVerticleTest extends HttpServerTest {

  private UserService mockUserService;

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    mockUserService = Mockito.mock(UserService.class);
    vertx.deployVerticle(
        new GrpcVerticle(new Config.GrpcConfig(port), mockUserService),
        testContext.succeedingThenComplete());
  }

  @Test
  void ping(Vertx vertx, VertxTestContext testContext) {
    GrpcClient.client(vertx)
        .request(socketAddress(), IamGrpc.getPingMethod())
        .compose(
            request -> {
              request.end(PingRequest.newBuilder().build());
              return request.response().compose(GrpcReadStream::last);
            })
        .onFailure(testContext::failNow)
        .onSuccess(
            reply -> {
              assertThat(reply.getMessage()).isEqualTo("pong");
              testContext.completeNow();
            });
  }

  @CsvSource({"true", "false"})
  @ParameterizedTest
  void checkSession(boolean isValid, Vertx vertx, VertxTestContext testContext) {
    var checkSessionRequest =
        CheckSessionRequest.newBuilder().setUserId("userId").setUserToken("token").build();

    when(mockUserService.isValidToken(
            checkSessionRequest.getUserId(), checkSessionRequest.getUserToken()))
        .thenReturn(Future.succeededFuture(isValid));

    GrpcClient.client(vertx)
        .request(socketAddress(), IamGrpc.getCheckSessionMethod())
        .compose(
            request -> {
              request.end(checkSessionRequest);
              return request.response().compose(GrpcReadStream::last);
            })
        .onFailure(testContext::failNow)
        .onSuccess(
            reply -> {
              assertThat(reply.getValid()).isEqualTo(isValid);
              testContext.completeNow();
            });
  }

  @Test
  void checkSessionExceptionally(Vertx vertx, VertxTestContext testContext) {
    var checkSessionRequest =
        CheckSessionRequest.newBuilder().setUserId("userId").setUserToken("token").build();

    when(mockUserService.isValidToken(
            checkSessionRequest.getUserId(), checkSessionRequest.getUserToken()))
        .thenReturn(Future.failedFuture(new Throwable("expected")));

    GrpcClient.client(vertx)
        .request(socketAddress(), IamGrpc.getCheckSessionMethod())
        .compose(
            request -> {
              request.end(checkSessionRequest);
              return request.response();
            })
        .onFailure(testContext::failNow)
        .onSuccess(
            response -> {
              assertThat(response.status()).isEqualTo(GrpcStatus.INTERNAL);
              testContext.completeNow();
            });
  }

  @AfterEach
  @DisplayName("Check that the verticle is still there")
  void lastChecks(Vertx vertx) {
    assertThat(vertx.deploymentIDs()).isNotEmpty().hasSize(1);
  }

  private SocketAddress socketAddress() {
    return SocketAddress.inetSocketAddress(port, "localhost");
  }
}
