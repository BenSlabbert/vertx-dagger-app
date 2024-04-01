/* Licensed under Apache-2.0 2024. */
package com.example.iam.proxyservice;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

@ProxyGen // Generate service proxies
@VertxGen // Generate the handler
interface RpcService {

  String ADDRESS = "RPC.ADDRESS";

  // provide some annotation here to generate a map with the name of the method as a string and the
  // set or required permissions as a key (String group, String role, Set<String> permissions) the
  // generated class can then be used by the authentication interceptor to check if the user has the
  // required permissions
  Future<String> getProtectedString(String request);
}
