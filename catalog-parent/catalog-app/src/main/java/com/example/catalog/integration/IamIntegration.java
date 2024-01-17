/* Licensed under Apache-2.0 2023. */
package com.example.catalog.integration;

import com.example.commons.config.Config;
import com.example.iam.rpc.api.CheckTokenRequest;
import com.example.iam.rpc.api.CheckTokenResponse;
import com.example.iam.rpc.api.IamRpcService;
import io.vertx.core.Future;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class IamIntegration implements AuthenticationIntegration {

  private final IamRpcService iamRpcService;

  @Inject
  IamIntegration(
      IamRpcService iamRpcService,
      Map<Config.ServiceIdentifier, Config.ServiceRegistryConfig>
          serviceIdentifierServiceRegistryConfigMap) {

    Config.ServiceRegistryConfig serviceRegistryConfig =
        serviceIdentifierServiceRegistryConfigMap.get(Config.ServiceIdentifier.IAM);

    if (null == serviceRegistryConfig) {
      throw new IllegalArgumentException("config cannot be null");
    }

    this.iamRpcService = iamRpcService;
  }

  public Future<CheckTokenResponse> isTokenValid(String token) {
    return iamRpcService.check(CheckTokenRequest.builder().token(token).build());
  }
}
