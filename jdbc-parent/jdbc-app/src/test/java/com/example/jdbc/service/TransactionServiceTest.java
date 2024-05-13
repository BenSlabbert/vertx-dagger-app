/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jdbc.TestBase;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class TransactionServiceTest extends TestBase {

  @Test
  void test() {
    TransactionService service = provider.transactionService();
    assertThat(service).isNotNull();
    service.test();
  }
}
