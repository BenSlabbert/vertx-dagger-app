/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import static com.example.commons.mesage.Headers.SAGA_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.commons.TestBase;
import com.example.commons.proto.v1.Proto;
import com.google.protobuf.GeneratedMessageV3;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.NoStackTraceException;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;

@Log
class SagaExecutorTest extends TestBase {

  @Test
  void testSuccess(Vertx vertx, VertxTestContext testContext) {
    Checkpoint commandMessageReceived = testContext.checkpoint(2);

    SagaStageHandler stageHandler1 =
        new SagaStageHandler() {

          @Override
          public Future<GeneratedMessageV3> getCommand(String sagaId) {
            return Future.succeededFuture(Proto.getDefaultInstance());
          }

          @Override
          public Future<Boolean> handleResult(String sagaId, Message<GeneratedMessageV3> result) {
            return Future.succeededFuture(true);
          }

          @Override
          public Future<Void> onRollBack(String sagaId) {
            return Future.succeededFuture();
          }
        };

    SagaStageHandler stageHandler2 =
        new SagaStageHandler() {

          @Override
          public Future<GeneratedMessageV3> getCommand(String sagaId) {
            return Future.succeededFuture(Proto.getDefaultInstance());
          }

          @Override
          public Future<Boolean> handleResult(String sagaId, Message<GeneratedMessageV3> result) {
            return Future.succeededFuture(true);
          }

          @Override
          public Future<Void> onRollBack(String sagaId) {
            return Future.succeededFuture();
          }
        };

    vertx
        .eventBus()
        .consumer(
            "CMD.1",
            msg -> {
              commandMessageReceived.flag();
              msg.reply(Proto.getDefaultInstance());
            });
    vertx
        .eventBus()
        .consumer(
            "CMD.2",
            msg -> {
              commandMessageReceived.flag();
              msg.reply(Proto.getDefaultInstance());
            });

    SagaExecutor sagaExecutor =
        provider
            .sagaBuilder()
            .withStage()
            .withCommandAddress("CMD.1")
            .withHandler(stageHandler1)
            .withStage()
            .withCommandAddress("CMD.2")
            .withHandler(stageHandler2)
            .build();

    sagaExecutor
        .execute()
        .onComplete(
            testContext.succeeding(
                sagaId ->
                    testContext.verify(
                        () -> {
                          assertThat(sagaId).isNotNull();
                          testContext.completeNow();
                        })));
  }

  @Test
  void testRollback_handlerError(Vertx vertx, VertxTestContext testContext) {
    Checkpoint commandMessageReceived = testContext.checkpoint(2);

    SagaStageHandler stageHandler1 =
        new SagaStageHandler() {

          @Override
          public Future<GeneratedMessageV3> getCommand(String sagaId) {
            return Future.succeededFuture(Proto.getDefaultInstance());
          }

          @Override
          public Future<Boolean> handleResult(String sagaId, Message<GeneratedMessageV3> result) {
            return Future.succeededFuture(true);
          }

          @Override
          public Future<Void> onRollBack(String sagaId) {
            return Future.succeededFuture();
          }
        };

    SagaStageHandler stageHandler2 =
        new SagaStageHandler() {

          @Override
          public Future<GeneratedMessageV3> getCommand(String sagaId) {
            return Future.succeededFuture(Proto.getDefaultInstance());
          }

          @Override
          public Future<Boolean> handleResult(String sagaId, Message<GeneratedMessageV3> result) {
            return Future.failedFuture(new NoStackTraceException("planned exception"));
          }

          @Override
          public Future<Void> onRollBack(String sagaId) {
            return Future.succeededFuture();
          }
        };

    SagaStageHandler stageHandler3 =
        new SagaStageHandler() {
          @Override
          public Future<GeneratedMessageV3> getCommand(String sagaId) {
            testContext.failNow("should not be called");
            return null;
          }

          @Override
          public Future<Boolean> handleResult(String sagaId, Message<GeneratedMessageV3> result) {
            testContext.failNow("should not be called");
            return null;
          }

          @Override
          public Future<Void> onRollBack(String sagaId) {
            testContext.failNow("should not be called");
            return null;
          }
        };

    vertx
        .eventBus()
        .consumer(
            "CMD.1",
            msg -> {
              commandMessageReceived.flag();
              msg.reply(Proto.getDefaultInstance());
            });
    vertx
        .eventBus()
        .consumer(
            "CMD.2",
            msg -> {
              commandMessageReceived.flag();
              msg.reply(Proto.getDefaultInstance());
            });

    vertx.eventBus().consumer("CMD.3", msg -> testContext.failNow("should not be called"));

    SagaExecutor sagaExecutor =
        provider
            .sagaBuilder()
            .withStage()
            .withCommandAddress("CMD.1")
            .withHandler(stageHandler1)
            .withStage()
            .withCommandAddress("CMD.2")
            .withHandler(stageHandler2)
            .withStage()
            .withCommandAddress("CMD.3")
            .withHandler(stageHandler3)
            .build();

    sagaExecutor
        .execute()
        .onComplete(
            testContext.failing(
                sagaId ->
                    testContext.verify(
                        () -> {
                          assertThat(sagaId).isNotNull();
                          testContext.completeNow();
                        })));
  }

  @Test
  void testRollback_consumerError(Vertx vertx, VertxTestContext testContext) {
    Checkpoint commandMessageReceived = testContext.checkpoint(3);

    SagaStageHandler stageHandler1 =
        new SagaStageHandler() {
          @Override
          public Future<GeneratedMessageV3> getCommand(String sagaId) {
            return Future.succeededFuture(Proto.getDefaultInstance());
          }

          @Override
          public Future<Boolean> handleResult(String sagaId, Message<GeneratedMessageV3> result) {
            return Future.succeededFuture(true);
          }

          @Override
          public Future<Void> onRollBack(String sagaId) {
            return Future.succeededFuture();
          }
        };

    SagaStageHandler stageHandler2 =
        new SagaStageHandler() {
          @Override
          public Future<GeneratedMessageV3> getCommand(String sagaId) {
            return Future.succeededFuture(Proto.getDefaultInstance());
          }

          @Override
          public Future<Boolean> handleResult(String sagaId, Message<GeneratedMessageV3> result) {
            return Future.succeededFuture(true);
          }

          @Override
          public Future<Void> onRollBack(String sagaId) {
            return Future.succeededFuture();
          }
        };

    SagaStageHandler stageHandler3 =
        new SagaStageHandler() {
          @Override
          public Future<GeneratedMessageV3> getCommand(String sagaId) {
            testContext.failNow("should not be called");
            return null;
          }

          @Override
          public Future<Boolean> handleResult(String sagaId, Message<GeneratedMessageV3> result) {
            testContext.failNow("should not be called");
            return null;
          }

          @Override
          public Future<Void> onRollBack(String sagaId) {
            testContext.failNow("should not be called");
            return null;
          }
        };

    vertx
        .eventBus()
        .consumer(
            "CMD.1",
            msg -> {
              System.err.println("handle CMD.1");
              commandMessageReceived.flag();
              msg.reply(Proto.getDefaultInstance());
            });

    AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    vertx
        .eventBus()
        .consumer(
            "CMD.2",
            msg -> {
              System.err.println("handle CMD.2");
              commandMessageReceived.flag();
              String sagaId = msg.headers().get(SAGA_ID_HEADER);

              if (atomicBoolean.compareAndSet(false, true)) {
                msg.fail(1, sagaId);
              } else {
                msg.reply(Proto.getDefaultInstance());
              }
            });

    vertx.eventBus().consumer("CMD.3", msg -> testContext.failNow("should not be called"));

    SagaExecutor sagaExecutor =
        provider
            .sagaBuilder()
            .withStage()
            .withCommandAddress("CMD.1")
            .withHandler(stageHandler1)
            .withStage()
            .withCommandAddress("CMD.2")
            .withHandler(stageHandler2)
            .withStage()
            .withCommandAddress("CMD.3")
            .withHandler(stageHandler3)
            .build();

    sagaExecutor
        .execute()
        .onComplete(
            testContext.failing(
                sagaId ->
                    testContext.verify(
                        () -> {
                          assertThat(sagaId).isNotNull();
                          testContext.completeNow();
                        })));
  }
}
