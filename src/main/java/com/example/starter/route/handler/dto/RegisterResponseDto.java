package com.example.starter.route.handler.dto;

import com.example.starter.json.JsonWriter;
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
