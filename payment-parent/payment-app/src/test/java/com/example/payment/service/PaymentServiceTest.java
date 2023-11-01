/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.payment.MockPersistenceTest;
import com.example.payment.repository.PaymentRepositoryImpl;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class PaymentServiceTest extends MockPersistenceTest {

  @Test
  void fetch() {
    when(paymentRepository.getPayments(dslContext))
        .thenReturn(
            Stream.of(
                new PaymentRepositoryImpl.Projection(1L, "name_1", 0L),
                new PaymentRepositoryImpl.Projection(2L, "name_2", 0L),
                new PaymentRepositoryImpl.Projection(3L, "name_3", 0L),
                new PaymentRepositoryImpl.Projection(4L, "name_4", 0L),
                new PaymentRepositoryImpl.Projection(5L, "name_5", 0L)));

    PaymentService paymentService = provider.paymentService();
    assertThat(paymentService.fetch()).hasSize(5);
  }

  @Test
  void save() {
    when(paymentRepository.save(dslContext, "name")).thenReturn(1L);

    PaymentService paymentService = provider.paymentService();

    Long newId = paymentService.save("name");

    assertThat(newId).isEqualTo(1L);
  }
}
