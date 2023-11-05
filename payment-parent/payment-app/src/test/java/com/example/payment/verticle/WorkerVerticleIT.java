/* Licensed under Apache-2.0 2023. */
package com.example.payment.verticle;

import static com.example.commons.kafka.common.Headers.SAGA_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.example.catalog.proto.saga.v1.CreatePaymentRequest;
import com.example.payment.VerticleTest;
import com.google.protobuf.GeneratedMessageV3;
import io.vertx.junit5.VertxTestContext;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.junit.jupiter.api.Test;

class WorkerVerticleIT extends VerticleTest {

  @Test
  void test(VertxTestContext testContext) {
    CreatePaymentRequest cmd = CreatePaymentRequest.newBuilder().setSagaId("sagaId").build();
    KafkaProducerRecord<String, GeneratedMessageV3> producerRecord =
        KafkaProducerRecord.create("Saga.Catalog.CreatePayment", "", cmd, 0);
    producerRecord.addHeader(SAGA_ID_HEADER, "sagaId");

    provider
        .producer()
        .send(producerRecord)
        .onComplete(
            ar -> {
              if (ar.failed()) {
                testContext.failNow(ar.cause());
              }
            });

    await()
        .untilAsserted(
            () -> {
              var fetch = provider.paymentService().fetch();
              assertThat(fetch).hasSize(1);
              testContext.completeNow();
            });
  }
}
