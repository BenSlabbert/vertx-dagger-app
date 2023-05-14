package com.example.catalog.web.route.dto;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;

public record FindAllResponseDto(List<FindOneResponseDto> dtos) implements JsonWriter {

  public static String ITEMS_FIELD = "items";

  public FindAllResponseDto(JsonObject jsonObject) {
    this(parse(jsonObject.getJsonArray(ITEMS_FIELD)));
  }

  private static List<FindOneResponseDto> parse(JsonArray jsonArray) {
    return jsonArray.stream()
        .filter(JsonObject.class::isInstance)
        .map(o -> (JsonObject) o)
        .map(FindOneResponseDto::new)
        .toList();
  }

  @Override
  public JsonObject toJson() {
    JsonArray array = new JsonArray();
    dtos.stream().map(FindOneResponseDto::toJson).forEach(array::add);
    return new JsonObject().put(ITEMS_FIELD, array);
  }
}
