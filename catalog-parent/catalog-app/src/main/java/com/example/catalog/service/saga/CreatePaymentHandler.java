/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service.saga;

import com.example.catalog.api.saga.CreatePurchaseOrderFailedResponse;
import com.example.catalog.api.saga.CreatePurchaseOrderRequest;
import com.example.catalog.api.saga.CreatePurchaseOrderResponse;
import com.example.catalog.api.saga.CreatePurchaseOrderSuccessResponse;
import com.example.commons.saga.SagaStageHandler;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
public class CreatePaymentHandler implements SagaStageHandler {

  private static final Logger log = LoggerFactory.getLogger(CreatePaymentHandler.class);

  @Override
  public Future<JsonObject> getCommand(String sagaId) {
    log.info("%s: getting command".formatted(sagaId));
    CreatePurchaseOrderRequest cmd = CreatePurchaseOrderRequest.builder().sagaId(sagaId).build();
    return Future.succeededFuture(cmd.toJson());
  }

  @Override
  public Future<Boolean> handleResult(String sagaId, Message<JsonObject> result) {
    log.info("%s: handle result".formatted(sagaId));

    CreatePurchaseOrderResponse response = CreatePurchaseOrderResponse.fromJson(result.body());
    ;

    if (response.getResponseCase() == CreatePurchaseOrderResponse.ResponseCase.SUCCESS) {
      CreatePurchaseOrderSuccessResponse success = response.successResponse();
      log.info("success: " + success);
      return Future.succeededFuture(true);
    }

    CreatePurchaseOrderFailedResponse failed = response.failedResponse();
    log.info("failure: " + failed);
    return Future.succeededFuture(false);
  }

  @Override
  public Future<Void> onRollBack(String sagaId) {
    log.info("%s: rollback".formatted(sagaId));
    return Future.succeededFuture();
  }
}
