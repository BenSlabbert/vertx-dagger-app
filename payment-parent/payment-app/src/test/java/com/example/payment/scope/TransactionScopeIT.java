/* Licensed under Apache-2.0 2023. */
package com.example.payment.scope;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.payment.PersistenceTest;
import com.example.payment.repository.PaymentRepositoryImpl;
import com.example.payment.scope.repo.Repo;
import io.vertx.core.VertxException;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransactionScopeIT extends PersistenceTest {

  @Test
  void testRollback() {
    TransactionComponent tc = provider.transactionComponentBuilder().build();
    try (var tm = tc.transactionManager()) {
      tm.startTransaction();

      Long newId = tc.repo().save("name1");
      assertThat(newId).isPositive();

      // implicit rollback called
      throw VertxException.noStackTrace("planned exception");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(VertxException.class).hasMessage("planned exception");
    }

    // need a new scope
    tc = provider.transactionComponentBuilder().build();
    try (var tm = tc.transactionManager()) {
      tm.startTransaction();
      Long newId = tc.repo().save("name2");
      assertThat(newId).isPositive();
      tm.commit();
    }

    assertThat(provider.paymentService().fetch())
        .singleElement()
        .satisfies(p -> assertThat(p.name()).isEqualTo("name2"));
  }

  @Test
  void testTransactionManagement() {
    TransactionComponent tc1 = provider.transactionComponentBuilder().build();
    TransactionComponent tc2 = provider.transactionComponentBuilder().build();

    TransactionManager t1 = tc1.transactionManager();
    TransactionManager t2 = tc2.transactionManager();

    t1.startTransaction();
    t2.startTransaction();

    Repo repo1 = tc1.repo();
    Repo repo2 = tc2.repo();

    Long id1 = repo1.save("name_1");
    assertThat(id1).isPositive();
    Long id2 = repo2.save("name_2");
    assertThat(id2).isPositive();

    // different transactions should be isolated from one another
    assertThat(repo1.fetch(id2)).isNull();
    assertThat(repo2.fetch(id1)).isNull();

    assertThat(repo1.fetchAll().toList())
        .singleElement()
        .satisfies(p -> assertThat(p.name()).isEqualTo("name_1"));

    assertThat(repo2.fetchAll().toList())
        .singleElement()
        .satisfies(p -> assertThat(p.name()).isEqualTo("name_2"));

    t1.commit();
    t2.commit();

    t1.close();
    t2.close();

    // once transactions are committed, they should be visible to one another
    List<PaymentRepositoryImpl.Projection> projections = provider.paymentService().fetch();
    assertThat(projections).hasSize(2);

    TransactionComponent transactionComponent = provider.transactionComponentBuilder().build();

    try (var txmgr = transactionComponent.transactionManager()) {
      txmgr.startTransaction();
      Repo repo = transactionComponent.repo();

      assertThat(repo.fetchAll().toList())
          .satisfiesExactlyInAnyOrder(
              p -> assertThat(p.name()).isEqualTo("name_1"),
              p -> assertThat(p.name()).isEqualTo("name_2"));
    }
  }
}
