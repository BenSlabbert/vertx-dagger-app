/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.service;

import com.example.jdbc.IntegrationTestBase;
import org.junit.jupiter.api.Test;

class NestedTransactionServiceIT extends IntegrationTestBase {

  @Test
  void test() {
    provider.nestedTransactionService().runInTransaction();
  }
}
