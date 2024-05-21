/* Licensed under Apache-2.0 2024. */
package com.example.catalog.api.saga;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record CreatePaymentSuccessResponse(String sagaId) {

  public static Builder builder() {
    return new AutoBuilder_CreatePaymentSuccessResponse_Builder();
  }

  public static CreatePaymentSuccessResponse fromJson(JsonObject json) {
    return CreatePaymentSuccessResponse_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return CreatePaymentSuccessResponse_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder sagaId(String sagaId);

    CreatePaymentSuccessResponse build();
  }
}
