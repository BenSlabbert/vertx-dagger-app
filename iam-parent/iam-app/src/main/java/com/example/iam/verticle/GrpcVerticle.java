package com.example.iam.verticle;

import com.example.commons.config.Config;
import com.example.iam.grpc.iam.CheckTokenResponse;
import com.example.iam.grpc.iam.IamGrpc;
import com.example.iam.service.TokenService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerRequest;
import java.util.logging.Level;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
public class GrpcVerticle extends AbstractVerticle {

  private final Config.GrpcConfig grpcConfig;
  private final TokenService tokenService;

  @Inject
  public GrpcVerticle(Config.GrpcConfig grpcConfig, TokenService tokenService) {
    this.grpcConfig = grpcConfig;
    this.tokenService = tokenService;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    log.log(
        Level.INFO,
        "starting grpc verticle on port: {0}",
        new Object[] {Integer.toString(grpcConfig.port())});

    var grpcServer = GrpcServer.server(vertx);

    grpcServer.callHandler(
        IamGrpc.getCheckTokenMethod(),
        request -> {
          request.handler(
              check ->
                  tokenService
                      .isValidToken(check.getToken())
                      .onSuccess(
                          valid ->
                              request
                                  .response()
                                  .end(CheckTokenResponse.newBuilder().setValid(true).build()))
                      .onFailure(
                          err ->
                              request
                                  .response()
                                  .end(CheckTokenResponse.newBuilder().setValid(false).build())));
          request.exceptionHandler(throwable -> setInternalStatusError(request, throwable));
        });

    vertx
        .createHttpServer(new HttpServerOptions().setPort(grpcConfig.port()).setHost("0.0.0.0"))
        .requestHandler(grpcServer)
        .listen(
            res -> {
              if (res.succeeded()) {
                log.info("started grpc server");
                startPromise.complete();
              } else {
                log.log(Level.SEVERE, "failed to start verticle", res.cause());
                startPromise.fail(res.cause());
              }
            });
  }

  private void setInternalStatusError(GrpcServerRequest<?, ?> request, Throwable throwable) {
    log.log(Level.SEVERE, "exception while handling request", throwable);
    request.response().status(GrpcStatus.INTERNAL).end();
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    log.warning("stopping");
    stopPromise.complete();
  }
}
