/* Licensed under Apache-2.0 2023. */
package com.example.catalog.web.route.dto;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;

public record PaginatedResponseDto(boolean more, long total, List<FindOneResponseDto> items)
    implements JsonWriter {

  public static String MORE_FIELD = "more";
  public static String TOTAL_FIELD = "total";
  public static String ITEMS_FIELD = "items";

  public PaginatedResponseDto(JsonObject jsonObject) {
    this(
        jsonObject.getBoolean(MORE_FIELD),
        jsonObject.getInteger(TOTAL_FIELD),
        parse(jsonObject.getJsonArray(ITEMS_FIELD)));
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
    items.stream().map(JsonWriter::toJson).forEach(array::add);
    return new JsonObject().put(MORE_FIELD, more).put(ITEMS_FIELD, array).put(TOTAL_FIELD, total);
  }
}
