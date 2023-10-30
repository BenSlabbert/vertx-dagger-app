/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import com.example.payment.PersistenceTest;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;

@Log
class PaymentServiceTest extends PersistenceTest {

  @Test
  void fetch() {
    persist(config -> provider.paymentRepository().save(config.dsl(), "seeded-payment"));

    PaymentService paymentService = provider.paymentService();
    paymentService.fetch();
  }

  @Test
  void save() {
    PaymentService paymentService = provider.paymentService();
    paymentService.save();
    paymentService.save();
    paymentService.save();
    paymentService.save();
    paymentService.save();
    paymentService.fetch();
  }
}
