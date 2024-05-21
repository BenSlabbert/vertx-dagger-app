/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service.saga;

import com.example.catalog.api.saga.CreatePaymentFailedResponse;
import com.example.catalog.api.saga.CreatePaymentRequest;
import com.example.catalog.api.saga.CreatePaymentResponse;
import com.example.catalog.api.saga.CreatePaymentSuccessResponse;
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
public class CreatePurchaseOrderHandler implements SagaStageHandler {

  private static final Logger log = LoggerFactory.getLogger(CreatePurchaseOrderHandler.class);

  @Override
  public Future<JsonObject> getCommand(String sagaId) {
    log.info("%s: getting command".formatted(sagaId));
    CreatePaymentRequest cmd = CreatePaymentRequest.builder().sagaId(sagaId).build();
    return Future.succeededFuture(cmd.toJson());
  }

  @Override
  public Future<Boolean> handleResult(String sagaId, Message<JsonObject> result) {
    log.info("%s: handle result".formatted(sagaId));

    CreatePaymentResponse response = CreatePaymentResponse.fromJson(result.body());
    ;

    if (response.getResponseCase() == CreatePaymentResponse.ResponseCase.SUCCESS) {
      CreatePaymentSuccessResponse success = response.successResponse();
      log.info("success: " + success);
      return Future.succeededFuture(true);
    }

    CreatePaymentFailedResponse failed = response.failedResponse();
    log.info("failure: " + failed);
    return Future.succeededFuture(false);
  }

  @Override
  public Future<Void> onRollBack(String sagaId) {
    log.info("%s: rollback".formatted(sagaId));
    return Future.succeededFuture();
  }
}
