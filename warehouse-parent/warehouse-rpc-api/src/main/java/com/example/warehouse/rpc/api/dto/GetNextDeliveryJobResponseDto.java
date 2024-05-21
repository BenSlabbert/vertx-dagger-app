/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.rpc.api.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import javax.annotation.Nullable;

@JsonWriter
public record GetNextDeliveryJobResponseDto(@Nullable Long deliveryId) {

  public static Builder builder() {
    return new AutoBuilder_GetNextDeliveryJobResponseDto_Builder();
  }

  public static GetNextDeliveryJobResponseDto fromJson(JsonObject json) {
    return GetNextDeliveryJobResponseDto_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return GetNextDeliveryJobResponseDto_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder deliveryId(@Nullable Long deliveryId);

    GetNextDeliveryJobResponseDto build();
  }
}
