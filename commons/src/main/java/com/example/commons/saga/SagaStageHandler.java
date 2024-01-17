/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public interface SagaStageHandler {

  /** send command to the command topic */
  Future<JsonObject> getCommand(String sagaId);

  /**
   * handle the result from the result topic
   *
   * <p>if false, a rollback command is sent to the command topic
   */
  Future<Boolean> handleResult(String sagaId, Message<JsonObject> result);

  /** in the event of a rollback this handler is called */
  Future<Void> onRollBack(String sagaId);
}
