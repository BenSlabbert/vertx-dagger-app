package com.example.catalog.web.route.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;

import com.example.commons.config.Config;
import com.example.iam.grpc.iam.CheckTokenRequest;
import com.example.iam.grpc.iam.IamGrpc;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.common.GrpcReadStream;
import java.util.Map;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class AuthHandler implements Handler<RoutingContext> {

  private final GrpcClient client;
  private final SocketAddress server;
  private final boolean disableSecurity;

  @Inject
  public AuthHandler(
      Vertx vertx,
      Map<Config.ServiceIdentifier, Config.ServiceRegistryConfig>
          serviceIdentifierServiceRegistryConfigMap) {
    this.disableSecurity = Boolean.parseBoolean(System.getenv("DISABLE_SECURITY"));
    this.client = GrpcClient.client(vertx);

    if (!disableSecurity) {
      log.info("security enabled, configuring iam service from registry");

      Config.ServiceRegistryConfig serviceRegistryConfig =
          serviceIdentifierServiceRegistryConfigMap.get(Config.ServiceIdentifier.IAM);

      if (null == serviceRegistryConfig) {
        throw new IllegalArgumentException("config cannot be null");
      }

      this.server =
          SocketAddress.inetSocketAddress(
              serviceRegistryConfig.port(), serviceRegistryConfig.host());
    } else {
      log.log(Level.WARNING, "security disabled, using local configs for iam service");
      this.server = SocketAddress.inetSocketAddress(50050, "localhost");
    }
  }

  @Override
  public void handle(RoutingContext ctx) {
    if (disableSecurity) {
      log.warning("security is disabled");
      ctx.next();
      return;
    }

    String authHeader = ctx.request().getHeader(HttpHeaders.AUTHORIZATION);

    if (null == authHeader) {
      ctx.fail(new HttpException(UNAUTHORIZED.code()));
      return;
    }

    client
        .request(server, IamGrpc.getCheckTokenMethod())
        .compose(
            request -> {
              request.end(CheckTokenRequest.newBuilder().setToken(authHeader).build());
              return request.response().compose(GrpcReadStream::last);
            })
        .onFailure(err -> ctx.fail(new HttpException(UNAUTHORIZED.code())))
        .onSuccess(
            reply -> {
              if (reply.getValid()) {
                ctx.next();
                return;
              }

              ctx.fail(new HttpException(UNAUTHORIZED.code()));
            });
  }
}
