/* Licensed under Apache-2.0 2023. */
package com.example.commons;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import java.util.function.Consumer;
import org.testcontainers.containers.output.OutputFrame;

public final class TestcontainerLogConsumer implements Consumer<OutputFrame> {

  private static final Logger log = LoggerFactory.getLogger(TestcontainerLogConsumer.class);

  private final String prefix;

  public TestcontainerLogConsumer(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public void accept(OutputFrame outputFrame) {
    switch (outputFrame.getType()) {
      case STDERR, STDOUT -> log.info(prefix + ": " + outputFrame.getUtf8String().trim());
    }
  }
}
