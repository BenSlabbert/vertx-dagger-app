/* Licensed under Apache-2.0 2024. */
package com.example.client.truck.service;

import com.example.client.truck.config.IamConfig;
import com.example.warehouse.rpc.api.WarehouseRpcServiceProviderFactory;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobResponseDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.LoginRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.RefreshRequestDto;
import github.benslabbert.vertxdaggerstarter.iamauthclient.IamAuthClient;
import github.benslabbert.vertxdaggerstarter.iamauthclient.IamAuthClientFactory;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.impl.jose.JWT;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class JobServiceImpl implements JobService {

  private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

  private final WarehouseRpcServiceProviderFactory warehouseRpcServiceProviderFactory;
  private final IamAuthClient iamAuthClient;
  private final IamConfig iamConfig;

  private volatile String token = null;
  private volatile String refreshToken = null;

  @Inject
  JobServiceImpl(
      IamConfig iamConfig,
      IamAuthClientFactory iamAuthClientFactory,
      WarehouseRpcServiceProviderFactory warehouseRpcServiceProviderFactory) {
    this.warehouseRpcServiceProviderFactory = warehouseRpcServiceProviderFactory;
    this.iamAuthClient = iamAuthClientFactory.create(iamConfig.host(), iamConfig.port());
    this.iamConfig = iamConfig;
  }

  @Override
  public void handle(Long timerId) {
    log.info("[timerId:{}] Checking for jobs", timerId);

    checkAuthenticated()
        .compose(ignore -> checkTokenValid())
        .compose(ignore -> getNetDelivery())
        .onSuccess(this::handle);
  }

  void handle(GetNextDeliveryJobResponseDto resp) {
    if (null == resp || null == resp.deliveryId()) {
      log.info("No jobs found");
      return;
    }

    log.info("Job found: {}", resp.deliveryId());
  }

  private Future<GetNextDeliveryJobResponseDto> getNetDelivery() {
    return warehouseRpcServiceProviderFactory
        .create(new DeliveryOptions().addHeader("auth-token", token))
        .get()
        .getNextDeliveryJob(GetNextDeliveryJobRequestDto.builder().truckId("truck-1").build());
  }

  private Future<Void> checkAuthenticated() {
    if (isAuthenticated()) {
      return Future.succeededFuture();
    }

    return iamAuthClient
        .login(
            LoginRequestDto.builder()
                .username(iamConfig.username())
                .password(iamConfig.password())
                .build())
        .onFailure(err -> log.error("failed to login", err))
        .map(
            r -> {
              token = r.token();
              refreshToken = r.refreshToken();
              return null;
            });
  }

  private Future<Void> checkTokenValid() {
    if (isTokenValid()) {
      return Future.succeededFuture();
    }

    return iamAuthClient
        .refresh(
            RefreshRequestDto.builder().username(iamConfig.username()).token(refreshToken).build())
        .onFailure(err -> log.error("failed to login", err))
        .map(
            r -> {
              token = r.token();
              refreshToken = r.refreshToken();
              return null;
            });
  }

  private boolean isTokenValid() {
    // not sure this is correct
    JsonObject parse = JWT.parse(token);
    JWTOptions jwtOptions = new JWTOptions(parse);
    int expiresInSeconds = jwtOptions.getExpiresInSeconds();
    // refresh the token if it expires in less than 30 seconds
    return expiresInSeconds > 30;
  }

  private boolean isAuthenticated() {
    return null != token && null != refreshToken;
  }
}
