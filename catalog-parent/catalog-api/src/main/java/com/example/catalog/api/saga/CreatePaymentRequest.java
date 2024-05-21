/* Licensed under Apache-2.0 2024. */
package com.example.catalog.api.saga;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record CreatePaymentRequest(String sagaId) {

  public static final String CREATE_PAYMENT_TOPIC = "Saga.Catalog.CreatePayment";

  public static Builder builder() {
    return new AutoBuilder_CreatePaymentRequest_Builder();
  }

  public static CreatePaymentRequest fromJson(JsonObject json) {
    return CreatePaymentRequest_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return CreatePaymentRequest_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    CreatePaymentRequest.Builder sagaId(String sagaId);

    CreatePaymentRequest build();
  }
}
