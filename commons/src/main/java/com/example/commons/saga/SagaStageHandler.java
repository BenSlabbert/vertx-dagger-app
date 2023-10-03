/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

public interface SagaStageHandler {

  /** send command to the command topic */
  String getCommand();

  /**
   * handle the result from the result topic
   *
   * <p>if false, a rollback command is sent to the command topic
   */
  boolean handleResult(String result);

  /** in the event of a rollback this handler is called */
  void onRollBack();
}
