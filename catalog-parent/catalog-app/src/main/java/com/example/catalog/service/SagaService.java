/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import com.example.catalog.api.saga.CreatePaymentRequest;
import com.example.catalog.api.saga.CreatePurchaseOrderRequest;
import com.example.catalog.service.saga.CreatePaymentHandler;
import com.example.catalog.service.saga.CreatePurchaseOrderHandler;
import com.example.commons.saga.SagaBuilder;
import com.example.commons.saga.SagaExecutor;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class SagaService {

  private final CreatePurchaseOrderHandler createPurchaseOrderHandler;
  private final CreatePaymentHandler createPaymentHandler;
  private final SagaBuilder sagaBuilder;

  @Inject
  SagaService(
      CreatePurchaseOrderHandler createPurchaseOrderHandler,
      CreatePaymentHandler createPaymentHandler,
      SagaBuilder sagaBuilder) {
    this.createPurchaseOrderHandler = createPurchaseOrderHandler;
    this.createPaymentHandler = createPaymentHandler;
    this.sagaBuilder = sagaBuilder;
  }

  public SagaExecutor createPurchaseOrderSaga() {
    return sagaBuilder
        .withStage()
        .withCommandAddress(CreatePurchaseOrderRequest.CREATE_PURCHASE_ORDER_TOPIC)
        .withHandler(createPurchaseOrderHandler)
        .withStage()
        .withCommandAddress(CreatePaymentRequest.CREATE_PAYMENT_TOPIC)
        .withHandler(createPaymentHandler)
        .build();
  }
}
