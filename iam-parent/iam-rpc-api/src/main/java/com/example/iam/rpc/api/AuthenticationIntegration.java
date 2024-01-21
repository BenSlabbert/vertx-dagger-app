/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

import io.vertx.core.Future;

public interface AuthenticationIntegration {

  Future<CheckTokenResponse> isTokenValid(String token);
}
