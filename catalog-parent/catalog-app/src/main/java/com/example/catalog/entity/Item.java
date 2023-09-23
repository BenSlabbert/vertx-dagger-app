/* Licensed under Apache-2.0 2023. */
package com.example.catalog.entity;

import static java.util.Objects.requireNonNull;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;

public record Item(long id, String name, long priceInCents) implements JsonWriter {

  public static String ID_FIELD = "id";
  public static String NAME_FIELD = "name";
  public static String PRICE_IN_CENTS_FIELD = "priceInCents";

  public Item(JsonObject jsonObject) {
    this(
        requireNonNull(jsonObject.getLong(ID_FIELD)),
        requireNonNull(jsonObject.getString(NAME_FIELD)),
        requireNonNull(jsonObject.getLong(PRICE_IN_CENTS_FIELD)));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject()
        .put(ID_FIELD, id)
        .put(NAME_FIELD, name)
        .put(PRICE_IN_CENTS_FIELD, priceInCents);
  }
}
