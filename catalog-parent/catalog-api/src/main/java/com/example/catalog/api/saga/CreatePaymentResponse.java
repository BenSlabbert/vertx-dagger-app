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
public class CreatePaymentResponse {

  public enum ResponseCase {
    SUCCESS,
    FAILURE
  }

  private String sagaId;

  private CreatePaymentFailedResponse failedResponse;

  private CreatePaymentSuccessResponse successResponse;

  public CreatePaymentResponse(JsonObject jsonObject) {
    CreatePaymentResponseConverter.fromJson(jsonObject, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    CreatePaymentResponseConverter.toJson(this, json);
    return json;
  }

  public ResponseCase getResponseCase() {
    if (failedResponse != null) {
      return ResponseCase.FAILURE;
    }
    if (successResponse != null) {
      return ResponseCase.SUCCESS;
    }
    throw new IllegalStateException("No response case set");
  }
}
