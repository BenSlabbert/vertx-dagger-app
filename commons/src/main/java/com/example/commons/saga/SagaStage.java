/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@Builder
@RequiredArgsConstructor
class SagaStage {

  private final String sagaId;
  private final String commandTopic;
  private final String resultTopic;
  private final SagaStageHandler handler;

  // todo this class needs to interact with kafka

  public void sendCommand() {
    String command = handler.getCommand();
    log.info("%s: sending command: %s".formatted(sagaId, command));
  }

  public void sendRollbackCommand() {
    log.info("%s: sending rollback command".formatted(sagaId));
    handler.onRollBack();
  }

  public boolean getResult() {
    log.info("%s: getting result".formatted(sagaId));
    // use the sagaId to filter for the result
    // if there is an error and the saga cannot continue, we must unwind
    return handler.handleResult("result");
  }
}
