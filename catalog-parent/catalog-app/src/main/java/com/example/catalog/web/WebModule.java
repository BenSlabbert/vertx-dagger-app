/* Licensed under Apache-2.0 2024. */
package com.example.catalog.web;

import dagger.Module;

@Module
public interface WebModule {

  SchemaValidatorDelegator schemaValidatorDelegator();
}
