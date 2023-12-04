/* Licensed under Apache-2.0 2023. */
package com.example.catalog.integration;

import dagger.Module;

@Module(includes = {IntegrationModuleBindings.class})
public interface IntegrationModule {

  AuthenticationIntegration authenticationIntegration();
}
