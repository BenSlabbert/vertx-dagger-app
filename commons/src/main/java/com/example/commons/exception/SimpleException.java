/* Licensed under Apache-2.0 2023. */
package com.example.commons.exception;

/** An exception without a stacktrace */
public class SimpleException extends RuntimeException {

  public SimpleException(String message) {
    super(message, null, true, false);
  }

  public SimpleException(String message, Throwable throwable) {
    super(message, throwable, true, false);
  }
}
