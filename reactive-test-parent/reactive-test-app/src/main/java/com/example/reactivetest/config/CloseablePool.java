package com.example.reactivetest.config;

import io.vertx.pgclient.PgPool;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class CloseablePool implements AutoCloseable {

  private final PgPool pool;

  @Inject
  CloseablePool(PgPool pool) {
    this.pool = pool;
  }

  @Override
  public void close() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    log.info("closing pg pool");
    pool.close()
        .onComplete(
            r -> {
              latch.countDown();

              if (r.failed()) {
                log.log(Level.SEVERE, "failed to close pool", r.cause());
              }
            });

    latch.await();
  }
}
