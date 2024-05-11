/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import static com.example.commons.mesage.Headers.SAGA_ID_HEADER;
import static com.example.commons.mesage.Headers.SAGA_ROLLBACK_HEADER;

import com.example.catalog.api.saga.CreatePurchaseOrderRequest;
import com.example.catalog.api.saga.CreatePurchaseOrderResponse;
import com.example.catalog.api.saga.CreatePurchaseOrderSuccessResponse;
import com.example.commons.mesage.Consumer;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
class CreatePaymentConsumer implements Consumer {

  private static final Logger log = LoggerFactory.getLogger(CreatePaymentConsumer.class);
  private static final String CMD_ADDRESS = CreatePurchaseOrderRequest.CREATE_PURCHASE_ORDER_TOPIC;

  private final Vertx vertx;

  private MessageConsumer<JsonObject> consumer;

  private void handle(Message<JsonObject> message) {
    log.info("handle message: %s".formatted(CMD_ADDRESS));

    MultiMap headers = message.headers();
    String sagaId = Objects.requireNonNull(headers.get(SAGA_ID_HEADER));

    if (null != headers.get(SAGA_ROLLBACK_HEADER)) {
      log.info("%s: received rollback".formatted(sagaId));
      // clean up db
      return;
    }

    CreatePurchaseOrderResponse response =
        CreatePurchaseOrderResponse.builder()
            .successResponse(CreatePurchaseOrderSuccessResponse.builder().sagaId(sagaId).build())
            .build();

    log.info("sending reply");
    message.reply(
        response,
        new DeliveryOptions()
            .addHeader(SAGA_ID_HEADER, sagaId)
            .addHeader(SAGA_ROLLBACK_HEADER, Boolean.TRUE.toString()));
  }

  @Override
  public void register() {
    consumer =
        vertx
            .eventBus()
            .consumer(CMD_ADDRESS, this::handle)
            .setMaxBufferedMessages(1_000)
            .exceptionHandler(err -> log.error("unhandled exception", err))
            .endHandler(ignore -> log.warn("read stream closed"));

    consumer.completionHandler(
        ar -> {
          if (ar.failed()) {
            log.error("failed to register consumer for address: " + CMD_ADDRESS, ar.cause());
            return;
          }

          log.info("successfully registered consumer for address: " + CMD_ADDRESS);
        });
  }

  @Override
  public Future<Void> unregister() {
    return consumer.unregister();
  }
}
