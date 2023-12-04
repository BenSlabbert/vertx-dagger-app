/* Licensed under Apache-2.0 2023. */
package com.example.catalog.integration;

import com.example.commons.config.Config;
import com.example.iam.grpc.iam.CheckTokenRequest;
import com.example.iam.grpc.iam.CheckTokenResponse;
import com.example.iam.grpc.iam.IamGrpc;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.common.GrpcReadStream;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class IamIntegration implements AuthenticationIntegration, AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(IamIntegration.class);

  private final GrpcClient client;
  private final SocketAddress server;

  @Inject
  IamIntegration(
      Vertx vertx,
      Map<Config.ServiceIdentifier, Config.ServiceRegistryConfig>
          serviceIdentifierServiceRegistryConfigMap) {
    this.client = GrpcClient.client(vertx);

    Config.ServiceRegistryConfig serviceRegistryConfig =
        serviceIdentifierServiceRegistryConfigMap.get(Config.ServiceIdentifier.IAM);

    if (null == serviceRegistryConfig) {
      throw new IllegalArgumentException("config cannot be null");
    }

    this.server =
        SocketAddress.inetSocketAddress(serviceRegistryConfig.port(), serviceRegistryConfig.host());
  }

  public Future<CheckTokenResponse> isTokenValid(String token) {
    return client
        .request(server, IamGrpc.getCheckTokenMethod())
        .compose(
            request -> {
              request.end(CheckTokenRequest.newBuilder().setToken(token).build());
              return request.response().compose(GrpcReadStream::last);
            });
  }

  @Override
  public void close() {
    log.info("closing iam grpc client");
    this.client.close();
  }
}
