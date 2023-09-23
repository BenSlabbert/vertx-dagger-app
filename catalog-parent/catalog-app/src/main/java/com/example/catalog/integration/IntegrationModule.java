/* Licensed under Apache-2.0 2023. */
package com.example.catalog.integration;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface IntegrationModule {

  @Binds
  AuthenticationIntegration toAuthenticationIntegration(IamIntegration iamIntegration);

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseable(IamIntegration iamIntegration);
}
