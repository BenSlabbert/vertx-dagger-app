/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service.saga;

import com.example.catalog.proto.saga.v1.CreatePaymentRequest;
import com.example.catalog.proto.saga.v1.CreatePaymentResponse;
import com.example.catalog.proto.saga.v1.CreatePaymentResponseFailedResponse;
import com.example.catalog.proto.saga.v1.CreatePaymentResponseSuccessResponse;
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
public class CreatePurchaseOrderHandler implements SagaStageHandler {

  @Override
  public Future<GeneratedMessageV3> getCommand(String sagaId) {
    log.info("%s: getting command".formatted(sagaId));
    CreatePaymentRequest cmd = CreatePaymentRequest.newBuilder().setSagaId(sagaId).build();
    return Future.succeededFuture(cmd);
  }

  @Override
  public Future<Boolean> handleResult(String sagaId, Message<GeneratedMessageV3> result) {
    log.info("%s: handle result".formatted(sagaId));

    byte[] bytes = result.body().toByteArray();

    CreatePaymentResponse response =
        ProtobufParser.parse(bytes, CreatePaymentResponse.getDefaultInstance());

    return switch (response.getResponseCase()) {
      case SUCCESS:
        log.info("success");
        CreatePaymentResponseSuccessResponse success = response.getSuccess();
        yield Future.succeededFuture(true);
      case FAILED, RESPONSE_NOT_SET:
        log.info("failure");
        CreatePaymentResponseFailedResponse failed = response.getFailed();
        yield Future.succeededFuture(false);
    };
  }

  @Override
  public Future<Void> onRollBack(String sagaId) {
    log.info("%s: rollback".formatted(sagaId));
    return Future.succeededFuture();
  }
}
