/* Licensed under Apache-2.0 2024. */
package com.example.starter.iam.auth.client;

import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface IamAuthClientFactory {

  IamAuthClient create(String baseUrl, int port);
}
