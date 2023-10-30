/* Licensed under Apache-2.0 2023. */
package com.example.payment.repository;

import static com.example.payment.generator.entity.generated.jooq.tables.Payment.PAYMENT;

import java.util.stream.Stream;
import javax.inject.Inject;
import org.jooq.DSLContext;

public class PaymentRepository {

  @Inject
  PaymentRepository() {}

  public Long save(DSLContext ctx, String name) {
    return ctx.insertInto(PAYMENT)
        .set(PAYMENT.NAME, name)
        .returning(PAYMENT.ID)
        .fetchOne(PAYMENT.ID);
  }

  public Stream<Projection> getPayments(DSLContext ctx) {
    return ctx.select(PAYMENT.ID, PAYMENT.VERSION)
        .from(PAYMENT)
        .fetchStream()
        .map(r -> new Projection(r.get(PAYMENT.ID), r.get(PAYMENT.VERSION)));
  }

  public record Projection(long id, long version) {}
}
