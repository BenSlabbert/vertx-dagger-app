package com.example.starter;

import java.util.function.Consumer;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.testcontainers.containers.output.OutputFrame;

@Log
public class TestcontainerLogConsumer implements Consumer<OutputFrame> {

  @Override
  public void accept(OutputFrame outputFrame) {
    switch (outputFrame.getType()) {
      case STDERR, STDOUT -> log.log(Level.INFO, outputFrame.getUtf8String());
    }
  }
}
