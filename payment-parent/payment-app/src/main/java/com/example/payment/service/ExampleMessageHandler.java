/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import static com.example.commons.kafka.common.Headers.SAGA_ID_HEADER;
import static com.example.commons.kafka.common.Headers.SAGA_ROLLBACK_HEADER;

import com.example.catalog.proto.saga.v1.CreatePaymentResponse;
import com.example.catalog.proto.saga.v1.CreatePaymentResponseFailedResponse;
import com.example.catalog.proto.saga.v1.CreatePaymentResponseSuccessResponse;
import com.example.commons.kafka.consumer.ConsumerUtils;
import com.example.commons.kafka.consumer.MessageHandler;
import com.google.protobuf.GeneratedMessageV3;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
public class ExampleMessageHandler implements MessageHandler {

  private static final Logger log = LoggerFactory.getLogger(ExampleMessageHandler.class);
  private static final String CMD_TOPIC = "Saga.Catalog.CreatePayment";
  private static final String REPLY_TOPIC = "Saga.Catalog.CreatePayment.Reply";

  private final Vertx vertx;
  private final KafkaProducer<String, GeneratedMessageV3> producer;
  private final PaymentService paymentService;

  @Override
  public String getTopic() {
    return CMD_TOPIC;
  }

  @Override
  public void handle(KafkaConsumerRecord<String, Buffer> message) {
    log.info("handle message: %s".formatted(CMD_TOPIC));
    log.info("handle message on thread: %s".formatted(Thread.currentThread().getName()));

    boolean workerContext = vertx.getOrCreateContext().isWorkerContext();
    boolean eventLoopContext = vertx.getOrCreateContext().isEventLoopContext();
    log.info("workerContext: %b, eventLoopContext: %b".formatted(workerContext, eventLoopContext));

    Map<String, Buffer> headers = ConsumerUtils.headersAsMap(message.headers());
    String sagaId = Objects.requireNonNull(headers.getOrDefault(SAGA_ID_HEADER, null)).toString();

    if (null != headers.get(SAGA_ROLLBACK_HEADER)) {
      log.info("%s: received rollback".formatted(sagaId));
      // clean up db
      return;
    }

    try {
      Long newId = paymentService.save("name");
      log.info("%s: created new payment: %d".formatted(sagaId, newId));
      sendSuccess(sagaId);
    } catch (Exception e) {
      log.error("failed to handle message for saga: %s".formatted(sagaId), e);
      sendFailure(sagaId);
    }
  }

  private void sendFailure(String sagaId) {
    CreatePaymentResponse response =
        CreatePaymentResponse.newBuilder()
            .setFailed(CreatePaymentResponseFailedResponse.newBuilder().setSagaId(sagaId).build())
            .build();

    log.info("sending failure reply: %s".formatted(REPLY_TOPIC));
    send(sagaId, response);
  }

  private void sendSuccess(String sagaId) {
    CreatePaymentResponse response =
        CreatePaymentResponse.newBuilder()
            .setSuccess(CreatePaymentResponseSuccessResponse.newBuilder().setSagaId(sagaId).build())
            .build();

    log.info("sending success reply: %s".formatted(REPLY_TOPIC));
    send(sagaId, response);
  }

  private void send(String sagaId, GeneratedMessageV3 response) {
    var producerRecord = KafkaProducerRecord.create(REPLY_TOPIC, "", response, 0);
    producerRecord.addHeader(SAGA_ID_HEADER, sagaId);

    producer
        .send(producerRecord)
        .onFailure(err -> log.error("failed to send message to kafka", err))
        .onSuccess(metadata -> log.info("sent to topic: " + metadata.getTopic()));
  }
}
