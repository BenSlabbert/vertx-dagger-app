/* Licensed under Apache-2.0 2023. */
package com.example.catalog.web.route.dto;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;

public record DeleteOneResponseDto() implements JsonWriter {

  public DeleteOneResponseDto(JsonObject jsonObject) {
    this();
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject();
  }
}
