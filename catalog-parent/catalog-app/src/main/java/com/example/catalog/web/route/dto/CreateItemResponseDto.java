/* Licensed under Apache-2.0 2023. */
package com.example.catalog.web.route.dto;

import io.vertx.core.json.JsonObject;
import java.util.Objects;

public record CreateItemResponseDto(long id, String name, long priceInCents, long version) {

  public static String ID_FIELD = "id";
  public static String NAME_FIELD = "name";
  public static String PRICE_IN_CENTS_FIELD = "priceInCents";
  public static String VERSION_FIELD = "version";

  public CreateItemResponseDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getLong(ID_FIELD)),
        Objects.requireNonNull(jsonObject.getString(NAME_FIELD)),
        Objects.requireNonNull(jsonObject.getLong(PRICE_IN_CENTS_FIELD)),
        Objects.requireNonNull(jsonObject.getLong(VERSION_FIELD)));
  }

  public JsonObject toJson() {
    return new JsonObject()
        .put(ID_FIELD, id)
        .put(NAME_FIELD, name)
        .put(PRICE_IN_CENTS_FIELD, priceInCents)
        .put(VERSION_FIELD, version);
  }
}
