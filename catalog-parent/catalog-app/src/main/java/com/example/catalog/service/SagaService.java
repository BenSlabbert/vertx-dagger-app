/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import com.example.catalog.service.saga.CreatePaymentHandler;
import com.example.catalog.service.saga.CreatePurchaseOrderHandler;
import com.example.commons.saga.SagaBuilder;
import com.example.commons.saga.SagaExecutor;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
public class SagaService {

  private static final String CREATE_PURCHASE_ORDER_TOPIC = "Saga.Catalog.CreatePurchaseOrder";
  private static final String CREATE_PAYMENT_TOPIC = "Saga.Catalog.CreatePayment";

  private final CreatePurchaseOrderHandler createPurchaseOrderHandler;
  private final CreatePaymentHandler createPaymentHandler;
  private final SagaBuilder sagaBuilder;

  public SagaExecutor createPurchaseOrderSaga() {
    return sagaBuilder
        .withStage()
        .withCommandAddress(CREATE_PURCHASE_ORDER_TOPIC)
        .withHandler(createPurchaseOrderHandler)
        .withStage()
        .withCommandAddress(CREATE_PAYMENT_TOPIC)
        .withHandler(createPaymentHandler)
        .build();
  }
}
