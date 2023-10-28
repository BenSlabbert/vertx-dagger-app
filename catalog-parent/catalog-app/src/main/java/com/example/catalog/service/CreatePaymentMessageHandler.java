/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import static com.example.commons.kafka.common.Headers.SAGA_ID_HEADER;
import static com.example.commons.kafka.common.Headers.SAGA_ROLLBACK_HEADER;

import com.example.catalog.proto.saga.v1.CreatePurchaseOrderResponse;
import com.example.catalog.proto.saga.v1.CreatePurchaseOrderSuccessResponse;
import com.example.commons.kafka.consumer.ConsumerUtils;
import com.example.commons.kafka.consumer.MessageHandler;
import com.google.protobuf.GeneratedMessageV3;
import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
public class CreatePaymentMessageHandler implements MessageHandler {

  private static final String CMD_TOPIC = "Saga.Catalog.CreatePurchaseOrder";
  private static final String REPLY_TOPIC = "Saga.Catalog.CreatePurchaseOrder.Reply";

  private final KafkaProducer<String, GeneratedMessageV3> producer;

  @Inject
  CreatePaymentMessageHandler(KafkaProducer<String, GeneratedMessageV3> producer) {
    this.producer = producer;
  }

  @Override
  public String getTopic() {
    return CMD_TOPIC;
  }

  @Override
  public void handle(KafkaConsumerRecord<String, Buffer> message) {
    log.info("handle message: %s".formatted(CMD_TOPIC));

    Map<String, Buffer> headers = ConsumerUtils.headersAsMap(message.headers());

    if (null != headers.get(SAGA_ROLLBACK_HEADER)) {
      log.info("received rollback message: " + message.value().toString());
      // clean up db
      return;
    }

    Buffer sagaId = headers.get(SAGA_ID_HEADER);

    CreatePurchaseOrderResponse response =
        CreatePurchaseOrderResponse.newBuilder()
            .setSuccess(
                CreatePurchaseOrderSuccessResponse.newBuilder()
                    .setSagaId(sagaId.toString())
                    .build())
            .build();

    KafkaProducerRecord<String, GeneratedMessageV3> producerRecord =
        KafkaProducerRecord.create(REPLY_TOPIC, "", response, 0);
    producerRecord.addHeader(SAGA_ID_HEADER, sagaId);
    producerRecord.addHeader(SAGA_ROLLBACK_HEADER, Boolean.TRUE.toString());

    log.info("sending reply: %s".formatted(REPLY_TOPIC));
    producer
        .send(producerRecord)
        .onFailure(err -> log.severe(err.getMessage()))
        .onSuccess(metadata -> log.info("sent to topic: " + metadata.getTopic()));
  }
}
