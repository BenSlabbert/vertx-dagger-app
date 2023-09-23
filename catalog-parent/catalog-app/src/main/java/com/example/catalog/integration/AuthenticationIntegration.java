/* Licensed under Apache-2.0 2023. */
package com.example.catalog.integration;

import io.vertx.core.Future;

public interface AuthenticationIntegration {

  Future<Boolean> isTokenValid(String token);
}
