/* Licensed under Apache-2.0 2023. */
package com.example.payment.repository;

import static com.example.payment.generator.entity.generated.jooq.tables.Payment.PAYMENT;

import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
public class PaymentRepositoryImpl implements PaymentRepository {

  @Inject
  PaymentRepositoryImpl() {}

  @Override
  public Long save(DSLContext ctx, String name) {
    return ctx.insertInto(PAYMENT)
        .set(PAYMENT.NAME, name)
        .returning(PAYMENT.ID)
        .fetchOne(PAYMENT.ID);
  }

  @Override
  public Stream<Projection> getPayments(DSLContext ctx) {
    return ctx.select(PAYMENT.ID, PAYMENT.NAME, PAYMENT.VERSION).from(PAYMENT).stream()
        .map(r -> new Projection(r.get(PAYMENT.ID), r.get(PAYMENT.NAME), r.get(PAYMENT.VERSION)));
  }

  public record Projection(long id, String name, long version) {}
}
