/* Licensed under Apache-2.0 2023. */
package com.example.commons;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Log
@ExtendWith(VertxExtension.class)
class VirtualThreadTest {

  private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

  @Test
  void virtualThreadVoidReturn(VertxTestContext testContext) {
    List<Future<Void>> tasks =
        Stream.generate(
                () ->
                    CompletableFuture.runAsync(
                        () -> log.info("from virtual thread"), executorService))
            .limit(1000L)
            .map(Future::fromCompletionStage)
            .toList();

    assertThat(tasks).hasSize(1000);

    Future.all(tasks).onComplete(testContext.succeedingThenComplete());
  }

  @Test
  void virtualThreadValueReturn(VertxTestContext testContext) {
    List<Future<String>> tasks =
        Stream.generate(
                () -> CompletableFuture.supplyAsync(() -> "from virtual thread", executorService))
            .limit(1000L)
            .map(Future::fromCompletionStage)
            .toList();

    assertThat(tasks).hasSize(1000);

    Future.all(tasks).onComplete(testContext.succeedingThenComplete());
  }
}
