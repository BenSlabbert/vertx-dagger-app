/* Licensed under Apache-2.0 2024. */
package com.example.client.truck.verticle;

import com.example.client.truck.config.IamConfig;
import com.example.client.truck.service.JobService;
import com.example.iam.auth.api.dto.LoginRequestDto;
import com.example.iam.auth.api.dto.LoginResponseDto;
import com.example.iam.auth.api.dto.RefreshRequestDto;
import com.example.starter.iam.auth.client.IamAuthClient;
import com.example.starter.iam.auth.client.IamAuthClientFactory;
import com.example.warehouse.rpc.api.WarehouseRpcService;
import com.example.warehouse.rpc.api.WarehouseRpcServiceProvider;
import com.example.warehouse.rpc.api.WarehouseRpcServiceProviderFactory;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobRequestDto;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import javax.inject.Inject;

public class TruckVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(TruckVerticle.class);

  private final WarehouseRpcServiceProviderFactory warehouseRpcServiceProviderFactory;
  private final IamAuthClientFactory iamAuthClientFactory;
  private final JobService jobService;
  private final IamConfig iamConfig;

  private volatile long timerId = 0L;

  @Inject
  TruckVerticle(
      WarehouseRpcServiceProviderFactory warehouseRpcServiceProviderFactory,
      IamAuthClientFactory iamAuthClientFactory,
      JobService jobService,
      IamConfig iamConfig) {
    this.warehouseRpcServiceProviderFactory = warehouseRpcServiceProviderFactory;
    this.iamAuthClientFactory = iamAuthClientFactory;
    this.jobService = jobService;
    this.iamConfig = iamConfig;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.exceptionHandler(err -> log.error("unhandled exception", err));

    IamAuthClient iamAuthClient = iamAuthClientFactory.create(iamConfig.host(), iamConfig.port());

    Future<LoginResponseDto> login =
        iamAuthClient.login(
            LoginRequestDto.builder()
                .username(iamConfig.username())
                .password(iamConfig.password())
                .build());

    login
        .onFailure(startPromise::fail)
        .onSuccess(
            resp -> {
              timerId = vertx.setPeriodic(1000L, 5000L, jobService);
              startPromise.complete();
            });

    iamAuthClient.refresh(
        RefreshRequestDto.builder()
            .token(login.result().refreshToken())
            .username(iamConfig.username())
            .build());

    WarehouseRpcServiceProvider provider =
        warehouseRpcServiceProviderFactory.create(
            new DeliveryOptions().addHeader("auth-token", "token"));

    WarehouseRpcService warehouseRpcService = provider.get();
    warehouseRpcService.getNextDeliveryJob(
        GetNextDeliveryJobRequestDto.builder().truckId("truck-1").build());

    log.info("starting TruckVerticle");

    startPromise.complete();
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    if (timerId != 0L) {
      boolean cancelled = vertx.cancelTimer(timerId);
      System.err.println("cancelled: " + cancelled);
    }
    stopPromise.complete();
  }
}
