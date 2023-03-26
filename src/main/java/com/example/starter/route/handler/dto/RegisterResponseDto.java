package com.example.starter.route.handler.dto;

import io.vertx.core.json.JsonObject;

public record RegisterResponseDto() {

  public RegisterResponseDto(JsonObject jsonObject) {
    this();
  }

  public JsonObject toJson() {
    return new JsonObject();
  }
}
