/* Licensed under Apache-2.0 2023. */
package com.example.codegen.generator.generator;

public class GenerationException extends RuntimeException {

  public GenerationException(Throwable cause) {
    super(cause);
  }

  public GenerationException(String message) {
    super(message);
  }
}
