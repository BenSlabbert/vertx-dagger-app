/* Licensed under Apache-2.0 2024. */
package com.example.catalog.api.saga;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record CreatePurchaseOrderFailedResponse(String sagaId) {

  public static Builder builder() {
    return new AutoBuilder_CreatePurchaseOrderFailedResponse_Builder();
  }

  public static CreatePurchaseOrderFailedResponse fromJson(JsonObject json) {
    return CreatePurchaseOrderFailedResponse_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return CreatePurchaseOrderFailedResponse_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder sagaId(String sagaId);

    CreatePurchaseOrderFailedResponse build();
  }
}
