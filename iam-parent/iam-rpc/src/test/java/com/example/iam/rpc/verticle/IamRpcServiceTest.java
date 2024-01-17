/* Licensed under Apache-2.0 2023. */
package com.example.iam.rpc.verticle;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.iam.rpc.TestBase;
import com.example.iam.rpc.api.CheckTokenRequest;
import com.example.iam.rpc.api.IamRpcService;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceException;
import org.junit.jupiter.api.Test;

class IamRpcServiceTest extends TestBase {

  @Test
  void checkSession(Vertx vertx, VertxTestContext testContext) {

    IamRpcService rpcService = IamRpcService.createClientProxy(vertx);

    rpcService
        .check(CheckTokenRequest.builder().token("blah").build())
        .onComplete(
            testContext.failing(
                err ->
                    testContext.verify(
                        () -> {
                          assertThat(err).isInstanceOf(ServiceException.class);
                          testContext.completeNow();
                        })));
  }
}
