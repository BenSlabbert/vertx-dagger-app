package com.example.starter;

import java.io.IOException;
import java.net.ServerSocket;

public abstract class HttpServerTest {

  private static int start = 0;
  protected final int port = setPort();

  private int setPort() {
    for (int i = 40_000 + start++; i < 50_000; i++) {
      try (var serverSocket = new ServerSocket(0)) {
        return serverSocket.getLocalPort();
      } catch (IOException e) {
        // do nothing
      }
    }
    throw new IllegalArgumentException("unable to find port");
  }
}
