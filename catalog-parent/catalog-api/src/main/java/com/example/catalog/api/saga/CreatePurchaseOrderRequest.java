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
public class CreatePurchaseOrderRequest {

  public static final String CREATE_PURCHASE_ORDER_TOPIC = "Saga.Catalog.CreatePurchaseOrder";

  private String sagaId;

  public CreatePurchaseOrderRequest(JsonObject jsonObject) {
    CreatePurchaseOrderRequestConverter.fromJson(jsonObject, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    CreatePurchaseOrderRequestConverter.toJson(this, json);
    return json;
  }
}
