/* Licensed under Apache-2.0 2023. */
package com.example.commons;

import java.io.IOException;
import java.net.ServerSocket;

public abstract class HttpServerTest {

  protected static int getPort() {
    for (int i = 0; i < 1000; i++) {
      try (var serverSocket = new ServerSocket(0)) {
        return serverSocket.getLocalPort();
      } catch (IOException e) {
        // do nothing
      }
    }
    throw new IllegalArgumentException("unable to find port");
  }
}
