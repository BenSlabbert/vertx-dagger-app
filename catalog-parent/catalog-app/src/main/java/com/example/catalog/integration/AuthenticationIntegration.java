/* Licensed under Apache-2.0 2023. */
package com.example.catalog.integration;

import com.example.iam.rpc.api.CheckTokenResponse;
import io.vertx.core.Future;

public interface AuthenticationIntegration {

  Future<CheckTokenResponse> isTokenValid(String token);
}
