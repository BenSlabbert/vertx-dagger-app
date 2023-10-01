/* Licensed under Apache-2.0 2023. */
package com.example.iam.verticle;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.iam.TestBase;
import com.example.iam.grpc.iam.CheckTokenRequest;
import com.example.iam.grpc.iam.IamGrpc;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.common.GrpcReadStream;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

class GrpcVerticleTest extends TestBase {

  @Test
  void checkSession(Vertx vertx, VertxTestContext testContext) {
    var checkSessionRequest = CheckTokenRequest.newBuilder().setToken("token").build();

    GrpcClient.client(vertx)
        .request(socketAddress(), IamGrpc.getCheckTokenMethod())
        .compose(
            request -> {
              request.end(checkSessionRequest);
              return request.response().compose(GrpcReadStream::last);
            })
        .onFailure(testContext::failNow)
        .onSuccess(
            reply -> {
              assertThat(reply.getValid()).isFalse();
              testContext.completeNow();
            });
  }

  private SocketAddress socketAddress() {
    return SocketAddress.inetSocketAddress(GRPC_PORT, "127.0.0.1");
  }
}
