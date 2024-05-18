/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jdbc.IntegrationTestBase;
import org.junit.jupiter.api.Test;

class TransactionServiceIT extends IntegrationTestBase {

  @Test
  void useCreatedTransaction() {
    TransactionService service = provider.transactionService();
    assertThat(service).isNotNull();
    service.useCreatedTransaction();
  }

  @Test
  void runInTransaction() {
    TransactionService service = provider.transactionService();
    assertThat(service).isNotNull();
    service.runInTransaction();
  }
}
