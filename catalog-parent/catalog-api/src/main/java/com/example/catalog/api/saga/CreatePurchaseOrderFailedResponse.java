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
public class CreatePurchaseOrderFailedResponse {

  private String sagaId;

  public CreatePurchaseOrderFailedResponse(JsonObject jsonObject) {
    CreatePurchaseOrderFailedResponseConverter.fromJson(jsonObject, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    CreatePurchaseOrderFailedResponseConverter.toJson(this, json);
    return json;
  }
}
