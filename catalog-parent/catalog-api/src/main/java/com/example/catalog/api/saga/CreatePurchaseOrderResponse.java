/* Licensed under Apache-2.0 2024. */
package com.example.catalog.api.saga;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import javax.annotation.Nullable;

@JsonWriter
public record CreatePurchaseOrderResponse(
    String sagaId,
    @Nullable CreatePurchaseOrderFailedResponse failedResponse,
    @Nullable CreatePurchaseOrderSuccessResponse successResponse) {

  public static Builder builder() {
    return new AutoBuilder_CreatePurchaseOrderResponse_Builder();
  }

  public static CreatePurchaseOrderResponse fromJson(JsonObject json) {
    return CreatePurchaseOrderResponse_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return CreatePurchaseOrderResponse_JsonWriter.toJson(this);
  }

  public CreatePurchaseOrderResponse {
    if (failedResponse == null && successResponse == null) {
      throw new IllegalStateException("Either failedResponse or successResponse must be set");
    }
  }

  @AutoBuilder
  public interface Builder {

    Builder sagaId(String sagaId);

    Builder failedResponse(@Nullable CreatePurchaseOrderFailedResponse failedResponse);

    Builder successResponse(@Nullable CreatePurchaseOrderSuccessResponse successResponse);

    CreatePurchaseOrderResponse build();
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
