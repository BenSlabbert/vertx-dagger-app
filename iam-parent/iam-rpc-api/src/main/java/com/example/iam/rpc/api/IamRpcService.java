/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

@ProxyGen // Generate service proxies
@VertxGen // Generate the handler
public interface IamRpcService {

  String ADDRESS = "RPC.IAM.TOKEN_CHECK";

  static IamRpcService createClientProxy(Vertx vertx) {
    return new IamRpcServiceVertxEBProxy(vertx, ADDRESS);
  }

  Future<CheckTokenResponse> check(CheckTokenRequest request);
}
