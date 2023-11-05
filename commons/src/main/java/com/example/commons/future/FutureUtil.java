/* Licensed under Apache-2.0 2023. */
package com.example.commons.future;

import io.vertx.core.Future;
import io.vertx.core.impl.NoStackTraceException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class FutureUtil {

  private FutureUtil() {}

  public static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

  public static <T> Future<T> run(Supplier<T> task) {
    CompletableFuture<T> completableFuture = CompletableFuture.supplyAsync(task, EXECUTOR);
    return Future.fromCompletionStage(completableFuture);
  }

  public static Future<Void> run(Runnable runnable) {
    CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(runnable, EXECUTOR);
    return Future.fromCompletionStage(completableFuture);
  }

  public static boolean awaitTermination() {
    try {
      return EXECUTOR.awaitTermination(30L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new NoStackTraceException(e);
    }
  }
}
