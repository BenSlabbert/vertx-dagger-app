/* Licensed under Apache-2.0 2024. */
package com.example.catalog.api.saga;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import javax.annotation.Nullable;

@JsonWriter
public record CreatePaymentResponse(
    String sagaId,
    @Nullable CreatePaymentFailedResponse failedResponse,
    @Nullable CreatePaymentSuccessResponse successResponse) {

  public static Builder builder() {
    return new AutoBuilder_CreatePaymentResponse_Builder();
  }

  public static CreatePaymentResponse fromJson(JsonObject json) {
    return CreatePaymentResponse_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return CreatePaymentResponse_JsonWriter.toJson(this);
  }

  public CreatePaymentResponse {
    if (failedResponse == null && successResponse == null) {
      throw new IllegalStateException("Either failedResponse or successResponse must be set");
    }
  }

  @AutoBuilder
  public interface Builder {

    Builder sagaId(String sagaId);

    Builder failedResponse(@Nullable CreatePaymentFailedResponse failedResponse);

    Builder successResponse(@Nullable CreatePaymentSuccessResponse successResponse);

    CreatePaymentResponse build();
  }

  public enum ResponseCase {
    SUCCESS,
    FAILURE
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
