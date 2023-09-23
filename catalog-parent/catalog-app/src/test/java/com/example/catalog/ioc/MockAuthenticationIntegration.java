/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.integration.AuthenticationIntegration;
import io.vertx.core.Future;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MockAuthenticationIntegration implements AuthenticationIntegration {

  @Inject
  public MockAuthenticationIntegration() {}

  @Override
  public Future<Boolean> isTokenValid(String token) {
    return Future.succeededFuture(true);
  }
}
