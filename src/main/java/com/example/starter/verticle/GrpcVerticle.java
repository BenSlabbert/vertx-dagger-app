package com.example.starter.verticle;

import com.example.starter.config.Config;
import com.example.starter.grpc.echo.EchoGrpc;
import com.example.starter.grpc.echo.EchoMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerResponse;
import java.util.logging.Level;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
public class GrpcVerticle extends AbstractVerticle {

  private final Config.GrpcConfig grpcConfig;

  @Inject
  public GrpcVerticle(Config.GrpcConfig grpcConfig) {
    this.grpcConfig = grpcConfig;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    log.log(
        Level.INFO,
        "starting grpc verticle on port: {0}",
        new Object[] {Integer.toString(grpcConfig.port())});

    GrpcServer grpcServer = GrpcServer.server(vertx);

    grpcServer.callHandler(
        EchoGrpc.getEchoMethod(),
        request ->
            request.handler(
                echo -> {
                  log.info("get an echo request");
                  GrpcServerResponse<EchoMessage, EchoMessage> response = request.response();
                  EchoMessage reply =
                      EchoMessage.newBuilder().setMessage(echo.getMessage()).build();
                  response.end(reply);
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
