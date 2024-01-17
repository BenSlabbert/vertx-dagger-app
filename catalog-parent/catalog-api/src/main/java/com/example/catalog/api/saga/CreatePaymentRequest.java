/* Licensed under Apache-2.0 2024. */
package com.example.catalog.api.saga;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

// vertx codegen annotations
@JsonGen
@DataObject
// lombok annotations
@Data
@Builder
@AllArgsConstructor
public class CreatePaymentRequest {

  public static final String CREATE_PAYMENT_TOPIC = "Saga.Catalog.CreatePayment";

  private String sagaId;

  public CreatePaymentRequest(JsonObject jsonObject) {
    CreatePaymentRequestConverter.fromJson(jsonObject, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    CreatePaymentRequestConverter.toJson(this, json);
    return json;
  }
}
