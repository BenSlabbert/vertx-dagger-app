/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import com.example.catalog.service.saga.CreatePaymentHandler;
import com.example.catalog.service.saga.CreatePurchaseOrderHandler;
import com.example.commons.kafka.producer.TopicCreatorFactory;
import com.example.commons.saga.SagaBuilder;
import com.example.commons.saga.SagaExecutor;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class SagaService {

  private static final String CREATE_PURCHASE_ORDER_TOPIC = "Saga.Catalog.CreatePurchaseOrder";
  private static final String CREATE_PURCHASE_ORDER_REPLY_TOPIC =
      "Saga.Catalog.CreatePurchaseOrder.Reply";
  private static final String CREATE_PAYMENT_TOPIC = "Saga.Catalog.CreatePayment";
  private static final String CREATE_PAYMENT_REPLY_TOPIC = "Saga.Catalog.CreatePayment.Reply";

  private final CreatePurchaseOrderHandler createPurchaseOrderHandler;
  private final CreatePaymentHandler createPaymentHandler;
  private final SagaBuilder sagaBuilder;

  @Inject
  SagaService(
      SagaBuilder sagaBuilder,
      TopicCreatorFactory topicCreatorFactory,
      CreatePurchaseOrderHandler createPurchaseOrderHandler,
      CreatePaymentHandler createPaymentHandler) {
    this.sagaBuilder = sagaBuilder;
    this.createPurchaseOrderHandler = createPurchaseOrderHandler;
    this.createPaymentHandler = createPaymentHandler;

    CompositeFuture all =
        Future.all(
            topicCreatorFactory.forTopic(CREATE_PURCHASE_ORDER_TOPIC).create(),
            topicCreatorFactory.forTopic(CREATE_PURCHASE_ORDER_REPLY_TOPIC).create(),
            topicCreatorFactory.forTopic(CREATE_PAYMENT_TOPIC).create(),
            topicCreatorFactory.forTopic(CREATE_PAYMENT_REPLY_TOPIC).create());

    all.onFailure(err -> log.log(Level.SEVERE, "failed to create topics", err))
        .onSuccess(ignore -> log.info("created topics"));
  }

  public SagaExecutor createPurchaseOrderSaga() {
    return sagaBuilder
        .withStage()
        .withTopics(CREATE_PURCHASE_ORDER_TOPIC, CREATE_PURCHASE_ORDER_REPLY_TOPIC)
        .withHandler(createPurchaseOrderHandler)
        .withStage()
        .withTopics(CREATE_PAYMENT_TOPIC, CREATE_PAYMENT_REPLY_TOPIC)
        .withHandler(createPaymentHandler)
        .build();
  }
}
