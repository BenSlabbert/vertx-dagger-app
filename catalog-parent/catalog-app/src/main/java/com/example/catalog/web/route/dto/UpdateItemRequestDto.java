/* Licensed under Apache-2.0 2023. */
package com.example.catalog.web.route.dto;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

public record UpdateItemRequestDto(String name, long priceInCents, long version)
    implements JsonWriter {

  public static String NAME_FIELD = "name";
  public static String PRICE_IN_CENTS_FIELD = "priceInCents";
  public static String VERSION_FIELD = "version";

  public UpdateItemRequestDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString(NAME_FIELD)),
        Objects.requireNonNull(jsonObject.getLong(PRICE_IN_CENTS_FIELD)),
        Objects.requireNonNull(jsonObject.getLong(VERSION_FIELD)));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject()
        .put(NAME_FIELD, name)
        .put(PRICE_IN_CENTS_FIELD, priceInCents)
        .put(VERSION_FIELD, version);
  }
}
