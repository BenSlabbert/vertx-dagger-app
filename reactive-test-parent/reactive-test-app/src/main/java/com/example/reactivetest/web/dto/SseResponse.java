/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.web.dto;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;

public record SseResponse(long id, String name) implements JsonWriter {

  public static String ID_FIELD = "id";
  public static String NAME_FIELD = "name";

  @Override
  public JsonObject toJson() {
    return new JsonObject().put(ID_FIELD, id).put(NAME_FIELD, name);
  }
}
