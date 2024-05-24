/* Licensed under Apache-2.0 2023. */
package com.example.iam.auth.api.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxdaggercommons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;

public record RegisterResponseDto() implements JsonWriter {

  public RegisterResponseDto(JsonObject ignore) {
    this();
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject();
  }

  public static Builder builder() {
    return new AutoBuilder_RegisterResponseDto_Builder();
  }

  @AutoBuilder
  public interface Builder {
    RegisterResponseDto build();
  }
}
