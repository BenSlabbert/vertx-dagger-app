/* Licensed under Apache-2.0 2023. */
package com.example.payment.repository;

import java.util.stream.Stream;
import org.jooq.DSLContext;

public interface PaymentRepository {

  Long save(DSLContext ctx, String name);

  Stream<PaymentRepositoryImpl.Projection> getPayments(DSLContext ctx);
}
