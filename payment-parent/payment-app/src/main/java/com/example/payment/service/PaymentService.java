/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import com.example.commons.transaction.blocking.TransactionBoundary;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.PaymentRepositoryImpl;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
public class PaymentService extends TransactionBoundary {

  private final PaymentRepository paymentRepository;

  @Inject
  PaymentService(DSLContext dslContext, PaymentRepository paymentRepository) {
    super(dslContext);
    this.paymentRepository = paymentRepository;
  }

  public Long save(String name) {
    return doInTransaction(ctx -> paymentRepository.save(ctx.dsl(), name));
  }

  public List<PaymentRepositoryImpl.Projection> fetch() {
    return doInTransaction(ctx -> paymentRepository.getPayments(ctx.dsl()).toList());
  }
}
