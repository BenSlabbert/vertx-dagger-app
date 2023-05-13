package com.example.iam.web.route.dto;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;

public record RegisterResponseDto() implements JsonWriter {

  @Override
  public JsonObject toJson() {
    return new JsonObject();
  }
}
