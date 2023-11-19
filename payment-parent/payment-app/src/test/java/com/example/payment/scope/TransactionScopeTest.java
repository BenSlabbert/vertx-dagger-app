/* Licensed under Apache-2.0 2023. */
package com.example.payment.scope;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.payment.MockPersistenceTest;
import com.example.payment.scope.repo.Repo;
import org.junit.jupiter.api.Test;

class TransactionScopeTest extends MockPersistenceTest {

  @Test
  void testProvidedComponentsAreDifferentInstances() {
    TransactionComponent tc1 = provider.transactionComponentBuilder().build();
    TransactionComponent tc2 = provider.transactionComponentBuilder().build();

    assertThat(tc1).isNotSameAs(tc2);
  }

  @Test
  void testProvidedComponentsSubComponentsAreDifferentInstances() {
    TransactionComponent tc1 = provider.transactionComponentBuilder().build();
    TransactionComponent tc2 = provider.transactionComponentBuilder().build();

    TransactionManager tm1 = tc1.transactionManager();
    TransactionManager tm2 = tc2.transactionManager();

    assertThat(tm1).isNotSameAs(tm2);
  }

  @Test
  void testProvidedComponentsSubComponentsAreSingleton() {
    TransactionComponent tc1 = provider.transactionComponentBuilder().build();

    TransactionManager tm1 = tc1.transactionManager();
    TransactionManager tm2 = tc1.transactionManager();

    assertThat(tm1).isSameAs(tm2);

    Repo r1 = tc1.repo();
    Repo r2 = tc1.repo();

    assertThat(r1).isSameAs(r2);
  }
}
