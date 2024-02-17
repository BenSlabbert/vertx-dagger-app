/* Licensed under Apache-2.0 2023. */
package com.example.codegen.generator.commons;

public class GenerationException extends RuntimeException {

  public GenerationException(Throwable cause) {
    super(cause);
  }

  public GenerationException(String message) {
    super(message);
  }
}
