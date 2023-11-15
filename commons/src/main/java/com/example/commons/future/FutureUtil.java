/* Licensed under Apache-2.0 2023. */
package com.example.commons.future;

import com.example.commons.thread.VirtualThreadFactory;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.NoStackTraceException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class FutureUtil {

  private FutureUtil() {}

  public static final ExecutorService EXECUTOR =
      Executors.newThreadPerTaskExecutor(VirtualThreadFactory.THREAD_FACTORY);

  public static <T> Future<T> run(Supplier<T> task) {
    CompletableFuture<T> completableFuture = CompletableFuture.supplyAsync(task, EXECUTOR);
    return Future.fromCompletionStage(completableFuture);
  }

  public static Future<Void> run(Runnable runnable) {
    CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(runnable, EXECUTOR);
    return Future.fromCompletionStage(completableFuture);
  }

  public static Future<Boolean> awaitTermination() {
    Promise<Boolean> promise = Promise.promise();

    Thread.ofVirtual()
        .start(
            () -> {
              try {
                EXECUTOR.shutdown();
                boolean ok = EXECUTOR.awaitTermination(10L, TimeUnit.SECONDS);
                promise.complete(ok);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new NoStackTraceException(e);
              }
            });

    return promise.future();
  }

  /** will block the current thread until the future is completed */
  @SuppressWarnings("java:S106") // logger generally not available during shutdown
  public static <T> void blockingExecution(Future<T> future) {
    CountDownLatch latch = new CountDownLatch(1);

    future.onComplete(
        ar -> {
          if (ar.failed()) {
            System.err.println("closing pg pool failed: " + ar.cause());
          }
          latch.countDown();
        });

    try {
      boolean await = latch.await(30L, TimeUnit.SECONDS);
      if (!await) {
        System.err.println("closing timed out");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.err.println("closing failed: " + e);
    }
  }
}
