/* Licensed under Apache-2.0 2023. */
package com.example.iam.rpc.verticle;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.iam.rpc.TestBase;
import com.example.iam.rpc.api.IamRpcService;
import com.example.iam.rpc.api.dto.CheckTokenRequestDto;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

class IamRpcServiceTest extends TestBase {

  @Test
  void checkSession(VertxTestContext testContext) {
    IamRpcService rpcService = provider.iamRpcService();

    rpcService
        .check(CheckTokenRequestDto.builder().token("blah").build())
        .onComplete(
            testContext.failing(
                err ->
                    testContext.verify(
                        () -> {
                          assertThat(err).isInstanceOf(IllegalStateException.class);
                          assertThat(err).hasMessageContaining("Invalid format for JWT");
                          testContext.completeNow();
                        })));
  }
}
