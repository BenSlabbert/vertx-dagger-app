/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtils {

  private FileUtils() {}

  public static void createRequiredDirectories(Path in) {
    try {
      Files.createDirectories(in);
    } catch (IOException e) {
      throw new FileUtilsException(e);
    }
  }

  private static class FileUtilsException extends RuntimeException {
    public FileUtilsException(Throwable cause) {
      super(cause);
    }
  }
}
