/* Licensed under Apache-2.0 2023. */
package com.example.payment.scope.repo;

import static com.example.payment.generator.entity.generated.jooq.tables.Payment.PAYMENT;

import com.example.payment.generator.entity.generated.jooq.tables.records.PaymentRecord;
import com.example.payment.scope.DslProvider;
import com.example.payment.scope.TransactionScope;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.jooq.DSLContext;

@TransactionScope
class RepoImpl implements Repo {

  private final DSLContext dsl;

  @Inject
  RepoImpl(DslProvider dslProvider) {
    this.dsl = dslProvider.getContext();
  }

  @Override
  public Stream<Projection> fetchAll() {
    return dsl.selectFrom(PAYMENT).stream().map(RepoImpl::map);
  }

  @Override
  public Projection fetch(long id) {
    return dsl.selectFrom(PAYMENT).where(PAYMENT.ID.eq(id)).fetchOne(RepoImpl::map);
  }

  @Override
  public Long save(String name) {
    return dsl.insertInto(PAYMENT)
        .set(PAYMENT.NAME, name)
        .returning(PAYMENT.ID)
        .fetchOne(PAYMENT.ID);
  }

  private static Projection map(PaymentRecord r) {
    return new Projection(r.getId(), r.getName(), r.getVersion());
  }
}
