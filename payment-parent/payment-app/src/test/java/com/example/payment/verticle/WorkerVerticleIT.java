/* Licensed under Apache-2.0 2023. */
package com.example.payment.verticle;

import com.example.payment.VerticleTest;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

class WorkerVerticleIT extends VerticleTest {

  @Test
  void test(VertxTestContext testContext) {
    testContext.completeNow();
    //    provider
    //        .producer()
    //        .send(producerRecord)
    //        .onComplete(
    //            ar -> {
    //              if (ar.failed()) {
    //                testContext.failNow(ar.cause());
    //              }
    //            });

    //    await()
    //        .untilAsserted(
    //            () -> {
    //              var fetch = provider.paymentService().fetch();
    //              assertThat(fetch).hasSize(1);
    //              testContext.completeNow();
    //            });
  }
}
