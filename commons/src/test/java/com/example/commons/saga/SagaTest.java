/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@Log
class SagaTest {

  @Test
  void testSuccess() {
    SagaStageHandler stageHandler1 = Mockito.mock(SagaStageHandler.class);
    SagaStageHandler stageHandler2 = Mockito.mock(SagaStageHandler.class);

    when(stageHandler1.getCommand()).thenReturn("");
    when(stageHandler1.handleResult("result")).thenReturn(true);

    when(stageHandler2.getCommand()).thenReturn("");
    when(stageHandler2.handleResult("result")).thenReturn(true);

    Saga saga =
        Saga.builder()
            .withStage()
            .withTopics("", "")
            .withHandler(stageHandler1)
            .withStage()
            .withTopics("", "")
            .withHandler(stageHandler2)
            .build();

    saga.execute()
        .onFailure(err -> fail("should not fail", err))
        .onSuccess(sagaId -> assertThat(sagaId).isNotNull());

    verify(stageHandler1).getCommand();
    verify(stageHandler1).handleResult("result");
    verify(stageHandler1, never()).onRollBack();

    verify(stageHandler2).getCommand();
    verify(stageHandler2).handleResult("result");
    verify(stageHandler2, never()).onRollBack();
  }

  @Test
  void testRollback() {
    SagaStageHandler stageHandler1 = Mockito.mock(SagaStageHandler.class);
    SagaStageHandler stageHandler2 = Mockito.mock(SagaStageHandler.class);
    SagaStageHandler stageHandler3 = Mockito.mock(SagaStageHandler.class);

    when(stageHandler1.getCommand()).thenReturn("");
    when(stageHandler1.handleResult("result")).thenReturn(true);
    doNothing().when(stageHandler1).onRollBack();

    when(stageHandler2.getCommand()).thenReturn("");
    when(stageHandler2.handleResult("result")).thenReturn(false);
    doNothing().when(stageHandler2).onRollBack();

    Saga saga =
        Saga.builder()
            .withStage()
            .withTopics("", "")
            .withHandler(stageHandler1)
            .withStage()
            .withTopics("", "")
            .withHandler(stageHandler2)
            .withStage()
            .withTopics("", "")
            .withHandler(stageHandler3)
            .build();

    saga.execute()
        .onFailure(err -> System.err.println("saga failed: " + err.getMessage()))
        .onSuccess(sagaId -> System.err.println("saga succeeded: " + sagaId));

    verify(stageHandler1).getCommand();
    verify(stageHandler1).handleResult("result");
    verify(stageHandler1).onRollBack();

    verify(stageHandler2).getCommand();
    verify(stageHandler2).handleResult("result");
    verify(stageHandler2).onRollBack();

    verifyNoInteractions(stageHandler3);
  }
}
