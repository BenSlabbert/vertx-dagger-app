/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.dto;

import com.example.commons.web.serialization.JsonWriter;
import com.google.auto.value.AutoBuilder;
import io.vertx.core.json.JsonObject;

public record UpdatePermissionsResponseDto() implements JsonWriter {

  public UpdatePermissionsResponseDto(JsonObject ignore) {
    this();
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject();
  }

  public static UpdatePermissionsResponseDto.Builder builder() {
    return new AutoBuilder_UpdatePermissionsResponseDto_Builder();
  }

  @AutoBuilder
  public interface Builder {
    UpdatePermissionsResponseDto build();
  }
}
