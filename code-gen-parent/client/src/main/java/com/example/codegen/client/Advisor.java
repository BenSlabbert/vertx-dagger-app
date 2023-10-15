/* Licensed under Apache-2.0 2023. */
package com.example.codegen.client;

import java.util.Optional;

public class Advisor implements AdvisorBeforeContract_Advised {

  @Override
  public boolean publicVoidMethod(Advised advised) {
    return false;
  }

  @Override
  public Optional<String> publicStringMethod(Advised advised) {
    return Optional.empty();
  }

  @Override
  public boolean publicVoidStringMethod(String in, Advised advised) {
    return false;
  }

  @Override
  public boolean publicVoidStringMethod(String in1, String in2, Advised advised) {
    return false;
  }

  @Override
  public Optional<String> publicStringMethod(String in, Advised advised) {
    return Optional.empty();
  }

  @Override
  public Optional<String> publicStringMethod(String in1, String in2, Advised advised) {
    return Optional.empty();
  }
}
