/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import com.example.commons.kafka.consumer.MessageHandler;
import com.google.protobuf.GeneratedMessageV3;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.kafka.client.producer.KafkaHeader;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@Builder
@RequiredArgsConstructor
class SagaStage implements MessageHandler {

  private static final String SAGA_ID_HEADER = "X-Saga-Id";
  private static final String SAGA_ROLLBACK_HEADER = "X-Saga-Rollback";

  private final String commandTopic;
  private final String resultTopic;
  private final SagaStageHandler handler;
  private final KafkaProducer<String, GeneratedMessageV3> producer;

  private final Map<String, Promise<Boolean>> promiseForSagaId = new ConcurrentHashMap<>();

  public Future<Void> sendCommand(Promise<Boolean> promise, String sagaId) {
    System.err.println("%s: sending command".formatted(sagaId));

    return handler
        .getCommand(sagaId)
        .compose(
            message -> {
              KafkaProducerRecord<String, GeneratedMessageV3> producerRecord =
                  KafkaProducerRecord.create(commandTopic, "", message, 0);
              producerRecord.addHeader(SAGA_ID_HEADER, sagaId);
              return producer.send(producerRecord);
            })
        .map(
            metadata -> {
              System.err.println(
                  "%s: sent to command topic %s with offset: %d"
                      .formatted(sagaId, commandTopic, metadata.getOffset()));

              System.err.println("waiting for promise: " + this);
              promiseForSagaId.put(sagaId, promise);
              return null;
            });
  }

  public Future<Void> sendRollbackCommand(String sagaId) {
    System.err.println("%s: sending rollback command".formatted(sagaId));

    return handler
        .onRollBack(sagaId)
        .compose(
            ignore -> {
              KafkaProducerRecord<String, GeneratedMessageV3> producerRecord =
                  KafkaProducerRecord.create(commandTopic, "", null, 0);
              producerRecord.addHeader(SAGA_ID_HEADER, sagaId);
              producerRecord.addHeader(SAGA_ROLLBACK_HEADER, Boolean.TRUE.toString());
              return producer.send(producerRecord);
            })
        .map(
            metadata -> {
              System.err.println(
                  "%s: sent rollback to command topic %s".formatted(sagaId, commandTopic));
              return null;
            });
  }

  @Override
  public String getResultTopic() {
    return resultTopic;
  }

  @Override
  public void handle(KafkaConsumerRecord<String, Buffer> message) {

    Map<String, Buffer> headers =
        message.headers().stream().collect(Collectors.toMap(KafkaHeader::key, KafkaHeader::value));

    Buffer buffer = headers.get(SAGA_ID_HEADER);

    if (null == buffer) {
      log.warning("no saga header present");
      return;
    }

    String messageSagaId = buffer.toString();
    System.err.println("handling message: " + this);
    System.err.println("handling message for saga: " + messageSagaId);
    System.err.println("handling message for topic: " + resultTopic);

    Promise<Boolean> promise = promiseForSagaId.remove(messageSagaId);

    if (null == promise) {
      log.warning("no pending promise for sagaId: %s".formatted(messageSagaId));
      return;
    }

    System.err.println("%s: handling message".formatted(messageSagaId));

    handler
        .handleResult(messageSagaId, message)
        .onFailure(promise::fail)
        .onSuccess(promise::complete);
  }

  @Override
  public String toString() {
    return String.valueOf(super.hashCode());
  }
}
