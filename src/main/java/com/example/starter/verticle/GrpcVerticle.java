package com.example.starter.verticle;

import com.example.starter.config.Config;
import com.example.starter.grpc.echo.CheckSessionResponse;
import com.example.starter.grpc.echo.IamGrpc;
import com.example.starter.grpc.echo.PingResponse;
import com.example.starter.service.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.server.GrpcServer;
import java.util.logging.Level;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
public class GrpcVerticle extends AbstractVerticle {

  private final Config.GrpcConfig grpcConfig;
  private final UserService userService;

  @Inject
  public GrpcVerticle(Config.GrpcConfig grpcConfig, UserService userService) {
    this.grpcConfig = grpcConfig;
    this.userService = userService;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    log.log(
        Level.INFO,
        "starting grpc verticle on port: {0}",
        new Object[] {Integer.toString(grpcConfig.port())});

    var grpcServer = GrpcServer.server(vertx);

    grpcServer
        .callHandler(
            IamGrpc.getPingMethod(),
            request ->
                request.handler(
                    echo -> {
                      log.info("get an echo request");
                      request.response().end(PingResponse.newBuilder().setMessage("pong").build());
                    }))
        .callHandler(
            IamGrpc.getCheckSessionMethod(),
            request ->
                request.handler(
                    check -> {
                      log.info("check session");
                      userService
                          .isValidToken(check.getUserId(), check.getUserToken())
                          .onSuccess(
                              valid -> {
                                var response =
                                    CheckSessionResponse.newBuilder().setValid(valid).build();
                                request.response().end(response);
                              })
                          .onFailure(
                              throwable -> {
                                log.log(Level.SEVERE, "unable to check request", throwable);
                                request.response().status(GrpcStatus.INTERNAL).end();
                              });
                    }));

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

  @Override
  public void stop(Promise<Void> stopPromise) {
    log.warning("stopping");
    stopPromise.complete();
  }
}
