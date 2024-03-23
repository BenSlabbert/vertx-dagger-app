/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.util;

import io.vertx.core.Future;
import io.vertx.ext.web.handler.HttpException;
import java.io.PrintStream;
import picocli.CommandLine;

public final class DisplayErrorUtil {

  private DisplayErrorUtil() {}

  public static int handleFailure(PrintStream out, Future<?> resp) {
    out.println("request failed");
    Throwable throwable = resp.cause();
    if (throwable instanceof HttpException e) {
      out.println("status code: " + e.getStatusCode());
    } else {
      out.println("error: " + throwable.getMessage());
    }
    return CommandLine.ExitCode.SOFTWARE;
  }
}
