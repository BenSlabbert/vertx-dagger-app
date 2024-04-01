/* Licensed under Apache-2.0 2024. */
package com.example.iam.proxyservice;

import io.vertx.core.Future;

class RpcServiceImpl implements RpcService {

  @Override
  public Future<String> getProtectedString(String request) {
    return switch (request) {
      case "admin" -> Future.succeededFuture("admin");
      case "user" -> Future.succeededFuture("user");
      default -> Future.succeededFuture(null);
    };
  }
}
