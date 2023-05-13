package com.example.catalog.web.route.dto;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;

public record FindOneRequestDto(long id) implements JsonWriter {

  public static String ID_FIELD = "id";

  @Override
  public JsonObject toJson() {
    return new JsonObject().put(ID_FIELD, id);
  }
}
