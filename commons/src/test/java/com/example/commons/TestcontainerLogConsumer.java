/* Licensed under Apache-2.0 2023. */
package com.example.commons;

import java.util.function.Consumer;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.testcontainers.containers.output.OutputFrame;

@Log
public final class TestcontainerLogConsumer implements Consumer<OutputFrame> {

  private final String prefix;

  public TestcontainerLogConsumer(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public void accept(OutputFrame outputFrame) {
    switch (outputFrame.getType()) {
      case STDERR, STDOUT -> log.log(
          Level.INFO, prefix + ": " + outputFrame.getUtf8String().trim());
    }
  }
}
