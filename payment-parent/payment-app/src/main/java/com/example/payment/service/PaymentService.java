/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import com.example.commons.transaction.blocking.TransactionBoundary;
import com.example.payment.repository.PaymentRepository;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.java.Log;
import org.jooq.DSLContext;

@Log
public class PaymentService extends TransactionBoundary {

  private final PaymentRepository paymentRepository;

  @Inject
  PaymentService(DSLContext dslContext, PaymentRepository paymentRepository) {
    super(dslContext);
    this.paymentRepository = paymentRepository;
  }

  public void save() {
    Long newId = doInTransaction(ctx -> paymentRepository.save(ctx.dsl(), "name"));
    log.info("saved payment with id: " + newId);
  }

  public void fetch() {
    List<PaymentRepository.Projection> projections =
        doInTransaction(ctx -> paymentRepository.getPayments(ctx.dsl()).toList());
    log.info("payments: " + projections);
  }
}
