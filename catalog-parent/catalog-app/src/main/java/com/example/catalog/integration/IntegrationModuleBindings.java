/* Licensed under Apache-2.0 2023. */
package com.example.catalog.integration;

import dagger.Binds;
import dagger.Module;

@Module
interface IntegrationModuleBindings {

  @Binds
  AuthenticationIntegration toAuthenticationIntegration(IamIntegration iamIntegration);
}
