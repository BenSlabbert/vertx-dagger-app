/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import static com.example.commons.mesage.Headers.SAGA_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.commons.TestBase;
import com.example.commons.proto.v1.Proto;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.NoStackTraceException;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@Log
class SagaExecutorTest extends TestBase {

  @Test
  void testSuccess(Vertx vertx, VertxTestContext testContext) {
    Checkpoint commandMessageReceived = testContext.checkpoint(2);

    SagaStageHandler stageHandler1 = Mockito.mock(SagaStageHandler.class);
    SagaStageHandler stageHandler2 = Mockito.mock(SagaStageHandler.class);

    when(stageHandler1.getCommand(anyString()))
        .thenReturn(Future.succeededFuture(Proto.getDefaultInstance()));
    when(stageHandler1.handleResult(anyString(), any())).thenReturn(Future.succeededFuture(true));

    when(stageHandler2.getCommand(anyString()))
        .thenReturn(Future.succeededFuture(Proto.getDefaultInstance()));
    when(stageHandler2.handleResult(anyString(), any())).thenReturn(Future.succeededFuture(true));

    // register consumer for command topics
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
  void testRollback_handlerError(Vertx vertx, VertxTestContext testContext) {
    Checkpoint commandMessageReceived = testContext.checkpoint(2);

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

  @Test
  void testRollback_consumerError(Vertx vertx, VertxTestContext testContext) {
    Checkpoint commandMessageReceived = testContext.checkpoint(2);

    SagaStageHandler stageHandler1 = Mockito.mock(SagaStageHandler.class);
    SagaStageHandler stageHandler2 = Mockito.mock(SagaStageHandler.class);
    SagaStageHandler stageHandler3 = Mockito.mock(SagaStageHandler.class);

    when(stageHandler1.getCommand(anyString()))
        .thenReturn(Future.succeededFuture(Proto.getDefaultInstance()));
    when(stageHandler1.handleResult(anyString(), any())).thenReturn(Future.succeededFuture(true));
    when(stageHandler1.onRollBack(anyString())).thenReturn(Future.succeededFuture());

    when(stageHandler2.getCommand(anyString()))
        .thenReturn(Future.succeededFuture(Proto.getDefaultInstance()));
    when(stageHandler2.onRollBack(anyString())).thenReturn(Future.succeededFuture());

    // register consumer for command topics
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
              String sagaId = msg.headers().get(SAGA_ID_HEADER);
              msg.fail(1, sagaId);
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
                          verify(stageHandler1).getCommand(anyString());
                          verify(stageHandler1).handleResult(anyString(), any());
                          verify(stageHandler1).onRollBack(anyString());

                          verify(stageHandler2).getCommand(anyString());
                          verify(stageHandler2, never()).handleResult(anyString(), any());
                          verify(stageHandler2).onRollBack(anyString());

                          verifyNoInteractions(stageHandler3);
                          testContext.completeNow();
                        })));
  }
}
