/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import static github.benslabbert.vertxdaggercommons.mesage.Headers.SAGA_ID_HEADER;
import static github.benslabbert.vertxdaggercommons.mesage.Headers.SAGA_ROLLBACK_HEADER;

import github.benslabbert.vertxdaggerapp.api.catalog.saga.CreatePaymentFailedResponse;
import github.benslabbert.vertxdaggerapp.api.catalog.saga.CreatePaymentRequest;
import github.benslabbert.vertxdaggerapp.api.catalog.saga.CreatePaymentResponse;
import github.benslabbert.vertxdaggerapp.api.catalog.saga.CreatePaymentSuccessResponse;
import github.benslabbert.vertxdaggercommons.mesage.Consumer;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CreatePaymentHandler implements Consumer {

  private static final Logger log = LoggerFactory.getLogger(CreatePaymentHandler.class);
  private static final String CMD_ADDRESS = CreatePaymentRequest.CREATE_PAYMENT_TOPIC;

  private final PaymentService paymentService;
  private final Vertx vertx;

  @Inject
  CreatePaymentHandler(Vertx vertx, PaymentService paymentService) {
    this.vertx = vertx;
    this.paymentService = paymentService;
  }

  private MessageConsumer<JsonObject> consumer;

  public void handle(Message<JsonObject> message) {
    log.info("handle message: {}", CMD_ADDRESS);
    log.info("handle message on thread: {}", Thread.currentThread().getName());

    ThreadingModel threadingModel = vertx.getOrCreateContext().threadingModel();
    boolean workerContext = vertx.getOrCreateContext().isWorkerContext();
    boolean eventLoopContext = vertx.getOrCreateContext().isEventLoopContext();
    log.info(
        "threadingModel: {}, workerContext: {}, eventLoopContext: {}",
        threadingModel,
        workerContext,
        eventLoopContext);

    MultiMap headers = message.headers();
    String sagaId = Objects.requireNonNull(headers.get(SAGA_ID_HEADER));

    if (null != headers.get(SAGA_ROLLBACK_HEADER)) {
      log.info("{}: received rollback", sagaId);
      // clean up db
      return;
    }

    try {
      Long newId = paymentService.save("name");
      log.info("{}: created new payment: {}", sagaId, newId);
      sendSuccess(sagaId, message);
    } catch (Exception e) {
      log.error("failed to handle message for saga: {}", sagaId, e);
      sendFailure(sagaId, message);
    }
  }

  private void sendFailure(String sagaId, Message<JsonObject> message) {
    JsonObject response =
        CreatePaymentResponse.builder()
            .failedResponse(CreatePaymentFailedResponse.builder().sagaId(sagaId).build())
            .build()
            .toJson();

    log.info("sending failure reply");
    send(sagaId, response, message);
  }

  private void sendSuccess(String sagaId, Message<JsonObject> message) {
    JsonObject response =
        CreatePaymentResponse.builder()
            .successResponse(CreatePaymentSuccessResponse.builder().sagaId(sagaId).build())
            .build()
            .toJson();

    log.info("sending success reply");
    send(sagaId, response, message);
  }

  private void send(String sagaId, JsonObject response, Message<JsonObject> message) {
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
