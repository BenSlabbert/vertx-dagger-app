package com.example.starter.web.route.dto;

import com.example.starter.web.JsonWriter;
import io.vertx.core.json.JsonObject;

public record RegisterResponseDto() implements JsonWriter {

  public RegisterResponseDto(JsonObject jsonObject) {
    this();
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject();
  }
}
