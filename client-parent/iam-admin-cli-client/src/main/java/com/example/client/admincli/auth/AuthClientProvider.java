/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.auth;

import com.example.client.admincli.config.IamConfig;
import com.example.starter.iam.auth.client.IamAuthClient;
import com.example.starter.iam.auth.client.IamAuthClientFactory;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthClientProvider implements AuthClientFactory {

  private final IamAuthClientFactory iamAuthClientFactory;
  private final IamConfig iamConfig;

  @Inject
  AuthClientProvider(IamConfig iamConfig, IamAuthClientFactory iamAuthClientFactory) {
    this.iamAuthClientFactory = iamAuthClientFactory;
    this.iamConfig = iamConfig;
  }

  @Override
  public IamAuthClient provide() {
    return iamAuthClientFactory.create(iamConfig.host(), iamConfig.port());
  }
}
