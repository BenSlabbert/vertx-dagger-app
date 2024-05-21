/* Licensed under Apache-2.0 2024. */
package com.example.catalog.api.saga;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record CreatePurchaseOrderSuccessResponse(String sagaId) {

  public static Builder builder() {
    return new AutoBuilder_CreatePurchaseOrderSuccessResponse_Builder();
  }

  public static CreatePurchaseOrderSuccessResponse fromJson(JsonObject json) {
    return CreatePurchaseOrderSuccessResponse_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return CreatePurchaseOrderSuccessResponse_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder sagaId(String sagaId);

    CreatePurchaseOrderSuccessResponse build();
  }
}
