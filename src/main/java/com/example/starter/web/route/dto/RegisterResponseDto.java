package com.example.starter.web.route.dto;

import com.example.starter.web.JsonWriter;
import io.vertx.core.json.JsonObject;

public record RegisterResponseDto() implements JsonWriter {

  @Override
  public JsonObject toJson() {
    return new JsonObject();
  }
}
