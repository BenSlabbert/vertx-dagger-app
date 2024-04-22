/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.payment.PersistenceTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class PaymentServiceIT extends PersistenceTest {

  @Test
  void fetch() {
    persist(config -> provider.paymentRepository().save(config.dsl(), "seeded-payment"));

    PaymentService paymentService = provider.paymentService();

    assertThat(paymentService.fetch())
        .singleElement()
        .satisfies(p -> assertThat(p.name()).isEqualTo("seeded-payment"));
  }

  @Test
  void save() {
    PaymentService paymentService = provider.paymentService();

    assertThat(paymentService.save("name")).isPositive();
    assertThat(paymentService.save("name")).isPositive();
    assertThat(paymentService.save("name")).isPositive();
    assertThat(paymentService.save("name")).isPositive();
    assertThat(paymentService.save("name")).isPositive();

    assertThat(paymentService.fetch())
        .hasSize(5)
        .allSatisfy(p -> assertThat(p.name()).isEqualTo("name"));
  }
}
