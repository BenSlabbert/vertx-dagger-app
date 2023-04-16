package com.example.starter.verticle;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.starter.HttpServerTest;
import com.example.starter.config.Config;
import com.example.starter.grpc.echo.EchoGrpc;
import com.example.starter.grpc.echo.EchoMessage;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.common.GrpcReadStream;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class GrpcVerticleTest extends HttpServerTest {

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(
        new GrpcVerticle(new Config.GrpcConfig(port)), testContext.succeedingThenComplete());
  }

  @Test
  void echo(Vertx vertx, VertxTestContext testContext) {
    GrpcClient client = GrpcClient.client(vertx);
    SocketAddress server = SocketAddress.inetSocketAddress(port, "localhost");
    client
        .request(server, EchoGrpc.getEchoMethod())
        .compose(
            request -> {
              request.end(EchoMessage.newBuilder().setMessage("message").build());
              return request.response().compose(GrpcReadStream::last);
            })
        .onFailure(testContext::failNow)
        .onSuccess(
            reply -> {
              assertThat(reply.getMessage()).isEqualTo("message");
              testContext.completeNow();
            });
  }

  @AfterEach
  @DisplayName("Check that the verticle is still there")
  void lastChecks(Vertx vertx) {
    assertThat(vertx.deploymentIDs()).isNotEmpty().hasSize(1);
  }
}
