/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.commons.KafkaTestBase;
import com.example.commons.kafka.consumer.MessageHandler;
import com.example.commons.kafka.proto.v1.Proto;
import com.google.protobuf.GeneratedMessageV3;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.NoStackTraceException;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaHeader;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.java.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@Log
class SagaExecutorTest extends KafkaTestBase {

  KafkaConsumer<String, Buffer> commandTopicConsumer;
  KafkaConsumer<String, Buffer> sagaConsumer;

  @BeforeEach
  void before() {
    commandTopicConsumer = provider.consumer();
    sagaConsumer = provider.consumer();
  }

  @AfterEach
  void after() {
    commandTopicConsumer.close();
    sagaConsumer.close();
  }

  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void testSuccess(VertxTestContext testContext) {
    SagaStageHandler stageHandler1 = Mockito.mock(SagaStageHandler.class);
    SagaStageHandler stageHandler2 = Mockito.mock(SagaStageHandler.class);

    when(stageHandler1.getCommand(anyString()))
        .thenReturn(Future.succeededFuture(Proto.getDefaultInstance()));
    when(stageHandler1.handleResult(anyString(), any())).thenReturn(Future.succeededFuture(true));

    when(stageHandler2.getCommand(anyString()))
        .thenReturn(Future.succeededFuture(Proto.getDefaultInstance()));
    when(stageHandler2.handleResult(anyString(), any())).thenReturn(Future.succeededFuture(true));

    // register consumer for command topics
    // we can echo null back as we mock the handlers
    commandTopicConsumer
        .handler(
            record -> {
              String topic = record.topic();
              System.err.println("handle topic command: " + topic);
              Map<String, String> headers =
                  record.headers().stream()
                      .collect(Collectors.toMap(KafkaHeader::key, h -> h.value().toString()));

              String sagaId = headers.get("X-Saga-Id");
              if (sagaId == null) {
                fail("no saga id header");
                return;
              }

              System.err.println("sagaId: " + sagaId);

              switch (topic) {
                case "CMD.1" -> {
                  System.err.println("sending reply to CMD.1.RES");
                  KafkaProducerRecord<String, GeneratedMessageV3> producerRecord =
                      KafkaProducerRecord.create("CMD.1.RES", "", Proto.getDefaultInstance(), 0);
                  producerRecord.addHeader("X-Saga-Id", sagaId);

                  provider
                      .producer()
                      .send(producerRecord)
                      .onFailure(testContext::failNow)
                      .onSuccess(m -> System.err.println("replied with offset: " + m.getOffset()));
                }
                case "CMD.2" -> {
                  System.err.println("sending reply to CMD.2.RES");
                  KafkaProducerRecord<String, GeneratedMessageV3> producerRecord =
                      KafkaProducerRecord.create("CMD.2.RES", "", Proto.getDefaultInstance(), 0);
                  producerRecord.addHeader("X-Saga-Id", sagaId);

                  provider
                      .producer()
                      .send(producerRecord)
                      .onFailure(testContext::failNow)
                      .onSuccess(m -> System.err.println("replied with offset: " + m.getOffset()));
                }
                default -> testContext.failNow("cannot handle: " + topic);
              }
            })
        .subscribe(Set.of("CMD.1", "CMD.2"))
        .onFailure(testContext::failNow)
        .onSuccess(ignore -> System.err.println("subscribed to topics successfully"));

    SagaExecutor sagaExecutor =
        provider
            .sagaBuilder()
            .withStage()
            .withTopics("CMD.1", "CMD.1.RES")
            .withHandler(stageHandler1)
            .withStage()
            .withTopics("CMD.2", "CMD.2.RES")
            .withHandler(stageHandler2)
            .build();

    Set<String> resulTopics =
        sagaExecutor.messageHandlers().stream()
            .map(MessageHandler::getResultTopic)
            .collect(Collectors.toSet());

    // register saga consumer for east result topic
    sagaConsumer
        .handler(
            msg -> {
              Map<String, MessageHandler> handlerForTopic =
                  sagaExecutor.messageHandlers().stream()
                      .collect(
                          Collectors.toMap(MessageHandler::getResultTopic, Function.identity()));

              MessageHandler handler = handlerForTopic.get(msg.topic());
              if (handler == null) {
                fail("no handler for topic: " + msg.topic());
                return;
              }

              handler.handle(msg);
            })
        .subscribe(resulTopics)
        .onFailure(testContext::failNow)
        .onSuccess(ignore -> System.err.println("subscribed to topics successfully"));

    sagaExecutor
        .execute()
        .onComplete(
            testContext.succeeding(
                sagaId ->
                    testContext.verify(
                        () -> {
                          System.err.println("asserting");
                          assertThat(sagaId).isNotNull();
                          verify(stageHandler1).getCommand(anyString());
                          verify(stageHandler1).handleResult(anyString(), any());
                          verify(stageHandler1, never()).onRollBack(anyString());

                          verify(stageHandler2).getCommand(anyString());
                          verify(stageHandler2).handleResult(anyString(), any());
                          verify(stageHandler2, never()).onRollBack(anyString());
                          testContext.completeNow();
                        })));
  }

  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void testRollback(VertxTestContext testContext) {
    SagaStageHandler stageHandler1 = Mockito.mock(SagaStageHandler.class);
    SagaStageHandler stageHandler2 = Mockito.mock(SagaStageHandler.class);
    SagaStageHandler stageHandler3 = Mockito.mock(SagaStageHandler.class);

    when(stageHandler1.getCommand(anyString()))
        .thenReturn(Future.succeededFuture(Proto.getDefaultInstance()));
    when(stageHandler1.handleResult(anyString(), any())).thenReturn(Future.succeededFuture(true));
    when(stageHandler1.onRollBack(anyString())).thenReturn(Future.succeededFuture());

    when(stageHandler2.getCommand(anyString()))
        .thenReturn(Future.succeededFuture(Proto.getDefaultInstance()));
    when(stageHandler2.handleResult(anyString(), any()))
        .thenReturn(Future.failedFuture(new NoStackTraceException("planned exception")));
    when(stageHandler2.onRollBack(anyString())).thenReturn(Future.succeededFuture());

    // register consumer for command topics
    // we can echo null back as we mock the handlers
    commandTopicConsumer
        .handler(
            record -> {
              String topic = record.topic();
              System.err.println("handle topic command: " + topic);
              Map<String, String> headers =
                  record.headers().stream()
                      .collect(Collectors.toMap(KafkaHeader::key, h -> h.value().toString()));

              String sagaId = headers.get("X-Saga-Id");
              if (sagaId == null) {
                fail("no saga id header");
                return;
              }

              System.err.println("sagaId: " + sagaId);

              if (headers.get("X-Saga-Rollback") != null) {
                System.err.println("rollback command, do nothing: " + topic);
                return;
              }

              switch (topic) {
                case "CMD.1" -> {
                  KafkaProducerRecord<String, GeneratedMessageV3> producerRecord =
                      KafkaProducerRecord.create("CMD.1.RES", "", Proto.getDefaultInstance(), 0);
                  producerRecord.addHeader("X-Saga-Id", sagaId);

                  provider.producer().send(producerRecord);
                }
                case "CMD.2" -> {
                  KafkaProducerRecord<String, GeneratedMessageV3> producerRecord =
                      KafkaProducerRecord.create("CMD.2.RES", "", Proto.getDefaultInstance(), 0);
                  producerRecord.addHeader("X-Saga-Id", sagaId);

                  provider.producer().send(producerRecord);
                }
                default -> fail("unknown topic: " + topic);
              }
            })
        .subscribe(Set.of("CMD.1", "CMD.2"))
        .onFailure(testContext::failNow)
        .onSuccess(ignore -> System.err.println("subscribed to topics successfully"));

    SagaExecutor sagaExecutor =
        provider
            .sagaBuilder()
            .withStage()
            .withTopics("CMD.1", "CMD.1.RES")
            .withHandler(stageHandler1)
            .withStage()
            .withTopics("CMD.2", "CMD.2.RES")
            .withHandler(stageHandler2)
            .withStage()
            .withTopics("CMD.3", "CMD.3.RES")
            .withHandler(stageHandler3)
            .build();

    Set<String> resulTopics =
        sagaExecutor.messageHandlers().stream()
            .map(MessageHandler::getResultTopic)
            .collect(Collectors.toSet());

    // register saga consumer for east result topic
    sagaConsumer
        .handler(
            record -> {
              Map<String, MessageHandler> handlerForTopic =
                  sagaExecutor.messageHandlers().stream()
                      .collect(
                          Collectors.toMap(MessageHandler::getResultTopic, Function.identity()));

              MessageHandler handler = handlerForTopic.get(record.topic());
              if (handler == null) {
                fail("no handler for topic: " + record.topic());
                return;
              }

              handler.handle(record);
            })
        .subscribe(resulTopics)
        .onFailure(testContext::failNow)
        .onSuccess(ignore -> System.err.println("subscribed to topics successfully"));

    sagaExecutor
        .execute()
        .onComplete(
            testContext.failing(
                sagaId ->
                    testContext.verify(
                        () -> {
                          verify(stageHandler1).getCommand(anyString());
                          verify(stageHandler1).handleResult(anyString(), any());
                          verify(stageHandler1).onRollBack(anyString());

                          verify(stageHandler2).getCommand(anyString());
                          verify(stageHandler2).handleResult(anyString(), any());
                          verify(stageHandler2).onRollBack(anyString());

                          verifyNoInteractions(stageHandler3);
                          testContext.completeNow();
                        })));
  }
}
