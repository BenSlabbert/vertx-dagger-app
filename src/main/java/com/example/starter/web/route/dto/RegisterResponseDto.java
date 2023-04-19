package com.example.starter.web.route.dto;

import io.vertx.core.json.JsonObject;

public record RegisterResponseDto() implements JsonWriter {

  @Override
  public JsonObject toJson() {
    return new JsonObject();
  }
}
