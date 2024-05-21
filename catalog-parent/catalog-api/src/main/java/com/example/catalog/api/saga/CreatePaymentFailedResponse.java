/* Licensed under Apache-2.0 2024. */
package com.example.catalog.api.saga;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record CreatePaymentFailedResponse(String sagaId) {

  public static Builder builder() {
    return new AutoBuilder_CreatePaymentFailedResponse_Builder();
  }

  public static CreatePaymentFailedResponse fromJson(JsonObject json) {
    return CreatePaymentFailedResponse_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return CreatePaymentFailedResponse_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder sagaId(String sagaId);

    CreatePaymentFailedResponse build();
  }
}
