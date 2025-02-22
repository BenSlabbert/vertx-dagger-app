/* Licensed under Apache-2.0 2023. */
package com.example.catalog.web.route.dto;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;

public record SuggestResponseDto(List<String> suggestions) {

  public static String SUGGESTIONS_FIELD = "suggestions";

  public SuggestResponseDto(JsonObject jsonObject) {
    this(parse(jsonObject.getJsonArray(SUGGESTIONS_FIELD)));
  }

  private static List<String> parse(JsonArray jsonArray) {
    return jsonArray.stream().map(Object::toString).toList();
  }

  public JsonObject toJson() {
    JsonArray array = new JsonArray(suggestions);
    return new JsonObject().put(SUGGESTIONS_FIELD, array);
  }
}
