/* Licensed under Apache-2.0 2024. */
package com.example.starter.iam.auth.client;

import dagger.Module;

@Module
public interface IamAuthClientModule {

  IamAuthClientFactory iamAuthClientFactory();
}
