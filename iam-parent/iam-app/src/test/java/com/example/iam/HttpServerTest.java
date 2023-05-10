package com.example.iam;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class HttpServerTest {

  private static final AtomicInteger INCREMENTER = new AtomicInteger(0);
  protected final int port = setPort();

  private int setPort() {
    for (int i = 40_000 + INCREMENTER.getAndIncrement(); i < 50_000; i++) {
      try (var serverSocket = new ServerSocket(0)) {
        return serverSocket.getLocalPort();
      } catch (IOException e) {
        // do nothing
      }
    }
    throw new IllegalArgumentException("unable to find port");
  }
}
