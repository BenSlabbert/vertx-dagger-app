/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import static com.example.commons.mesage.Headers.SAGA_ID_HEADER;
import static com.example.commons.mesage.Headers.SAGA_ROLLBACK_HEADER;

import com.example.catalog.proto.saga.v1.CreatePaymentResponse;
import com.example.catalog.proto.saga.v1.CreatePaymentResponseFailedResponse;
import com.example.catalog.proto.saga.v1.CreatePaymentResponseSuccessResponse;
import com.example.commons.mesage.Consumer;
import com.google.protobuf.GeneratedMessageV3;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
public class CreatePaymentHandler implements Consumer {

  private static final Logger log = LoggerFactory.getLogger(CreatePaymentHandler.class);
  private static final String CMD_ADDRESS = "Saga.Catalog.CreatePayment";

  private final PaymentService paymentService;
  private final Vertx vertx;

  private MessageConsumer<GeneratedMessageV3> consumer;

  public void handle(Message<GeneratedMessageV3> message) {
    log.info("handle message: %s".formatted(CMD_ADDRESS));
    log.info("handle message on thread: %s".formatted(Thread.currentThread().getName()));

    ThreadingModel threadingModel = vertx.getOrCreateContext().threadingModel();
    boolean workerContext = vertx.getOrCreateContext().isWorkerContext();
    boolean eventLoopContext = vertx.getOrCreateContext().isEventLoopContext();
    log.info(
        "threadingModel: %s, workerContext: %b, eventLoopContext: %b"
            .formatted(threadingModel, workerContext, eventLoopContext));

    MultiMap headers = message.headers();
    String sagaId = Objects.requireNonNull(headers.get(SAGA_ID_HEADER));

    if (null != headers.get(SAGA_ROLLBACK_HEADER)) {
      log.info("%s: received rollback".formatted(sagaId));
      // clean up db
      return;
    }

    try {
      Long newId = paymentService.save("name");
      log.info("%s: created new payment: %d".formatted(sagaId, newId));
      sendSuccess(sagaId, message);
    } catch (Exception e) {
      log.error("failed to handle message for saga: %s".formatted(sagaId), e);
      sendFailure(sagaId, message);
    }
  }

  private void sendFailure(String sagaId, Message<GeneratedMessageV3> message) {
    CreatePaymentResponse response =
        CreatePaymentResponse.newBuilder()
            .setFailed(CreatePaymentResponseFailedResponse.newBuilder().setSagaId(sagaId).build())
            .build();

    log.info("sending failure reply");
    send(sagaId, response, message);
  }

  private void sendSuccess(String sagaId, Message<GeneratedMessageV3> message) {
    CreatePaymentResponse response =
        CreatePaymentResponse.newBuilder()
            .setSuccess(CreatePaymentResponseSuccessResponse.newBuilder().setSagaId(sagaId).build())
            .build();

    log.info("sending success reply");
    send(sagaId, response, message);
  }

  private void send(
      String sagaId, GeneratedMessageV3 response, Message<GeneratedMessageV3> message) {
    message.reply(response, new DeliveryOptions().addHeader(SAGA_ID_HEADER, sagaId));
  }

  @Override
  public void register() {
    consumer = vertx.eventBus().consumer(CMD_ADDRESS, this::handle);

    consumer.setMaxBufferedMessages(1_000);

    consumer.completionHandler(
        ar -> {
          if (ar.failed()) {
            log.error("failed to register consumer for address: " + CMD_ADDRESS, ar.cause());
            return;
          }

          log.info("successfully registered consumer for address: " + CMD_ADDRESS);
        });

    consumer.exceptionHandler(err -> log.error("unhandled exception", err));

    consumer.endHandler(ignore -> log.warn("read stream closed"));
  }

  @Override
  public Future<Void> unregister() {
    return consumer.unregister();
  }
}
