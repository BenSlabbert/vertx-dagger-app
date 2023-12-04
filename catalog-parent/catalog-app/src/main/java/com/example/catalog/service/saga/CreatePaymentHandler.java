/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service.saga;

import com.example.catalog.proto.saga.v1.CreatePurchaseOrderFailedResponse;
import com.example.catalog.proto.saga.v1.CreatePurchaseOrderRequest;
import com.example.catalog.proto.saga.v1.CreatePurchaseOrderResponse;
import com.example.catalog.proto.saga.v1.CreatePurchaseOrderSuccessResponse;
import com.example.commons.protobuf.ProtobufParser;
import com.example.commons.saga.SagaStageHandler;
import com.google.protobuf.GeneratedMessageV3;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
public class CreatePaymentHandler implements SagaStageHandler {

  @Override
  public Future<GeneratedMessageV3> getCommand(String sagaId) {
    log.info("%s: getting command".formatted(sagaId));
    CreatePurchaseOrderRequest cmd =
        CreatePurchaseOrderRequest.newBuilder().setSagaId(sagaId).build();
    return Future.succeededFuture(cmd);
  }

  @Override
  public Future<Boolean> handleResult(String sagaId, Message<GeneratedMessageV3> result) {
    log.info("%s: handle result".formatted(sagaId));

    byte[] bytes = result.body().toByteArray();

    CreatePurchaseOrderResponse response =
        ProtobufParser.parse(bytes, CreatePurchaseOrderResponse.getDefaultInstance());

    return switch (response.getResponseCase()) {
      case SUCCESS:
        log.info("success");
        CreatePurchaseOrderSuccessResponse success = response.getSuccess();
        yield Future.succeededFuture(true);
      case FAILED, RESPONSE_NOT_SET:
        log.info("failure");
        CreatePurchaseOrderFailedResponse failed = response.getFailed();
        yield Future.succeededFuture(false);
    };
  }

  @Override
  public Future<Void> onRollBack(String sagaId) {
    log.info("%s: rollback".formatted(sagaId));
    return Future.succeededFuture();
  }
}
