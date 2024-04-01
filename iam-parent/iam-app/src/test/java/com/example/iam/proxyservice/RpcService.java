/* Licensed under Apache-2.0 2024. */
package com.example.iam.proxyservice;

import com.example.codegen.annotation.security.SecuredProxy;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

@SecuredProxy
@ProxyGen // Generate service proxies
@VertxGen // Generate the handler
interface RpcService {

  String ADDRESS = "RPC.ADDRESS";

  @SecuredProxy.SecuredAction(
      group = "group",
      role = "role",
      permissions = {"p1"})
  Future<String> getProtectedString(String request);
}
