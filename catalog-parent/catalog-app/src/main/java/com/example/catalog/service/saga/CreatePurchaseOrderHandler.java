/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service.saga;

import github.benslabbert.vertxdaggerapp.api.catalog.saga.CreatePaymentFailedResponse;
import github.benslabbert.vertxdaggerapp.api.catalog.saga.CreatePaymentRequest;
import github.benslabbert.vertxdaggerapp.api.catalog.saga.CreatePaymentResponse;
import github.benslabbert.vertxdaggerapp.api.catalog.saga.CreatePaymentSuccessResponse;
import github.benslabbert.vertxdaggercommons.saga.SagaStageHandler;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CreatePurchaseOrderHandler implements SagaStageHandler {

  private static final Logger log = LoggerFactory.getLogger(CreatePurchaseOrderHandler.class);

  @Inject
  CreatePurchaseOrderHandler() {}

  @Override
  public Future<JsonObject> getCommand(String sagaId) {
    log.info("{}: getting command", sagaId);
    CreatePaymentRequest cmd = CreatePaymentRequest.builder().sagaId(sagaId).build();
    return Future.succeededFuture(cmd.toJson());
  }

  @Override
  public Future<Boolean> handleResult(String sagaId, Message<JsonObject> result) {
    log.info("{}: handle result", sagaId);

    CreatePaymentResponse response = CreatePaymentResponse.fromJson(result.body());

    if (response.getResponseCase() == CreatePaymentResponse.ResponseCase.SUCCESS) {
      CreatePaymentSuccessResponse success = response.successResponse();
      log.info("success: {}", success);
      return Future.succeededFuture(true);
    }

    CreatePaymentFailedResponse failed = response.failedResponse();
    log.info("failure: {}", failed);
    return Future.succeededFuture(false);
  }

  @Override
  public Future<Void> onRollBack(String sagaId) {
    log.info("{}: rollback", sagaId);
    return Future.succeededFuture();
  }
}
