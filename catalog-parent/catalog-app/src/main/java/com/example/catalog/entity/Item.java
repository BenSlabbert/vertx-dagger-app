package com.example.catalog.entity;

import static java.util.Objects.requireNonNull;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.UUID;

public record Item(UUID id, String name, long priceInCents) implements JsonWriter {

  public static String ID_FIELD = "id";
  public static String NAME_FIELD = "name";
  public static String PRICE_IN_CENTS_FIELD = "priceInCents";

  public Item(JsonObject jsonObject) {
    this(
        requireNonNull(UUID.fromString(jsonObject.getString(ID_FIELD))),
        requireNonNull(jsonObject.getString(NAME_FIELD)),
        requireNonNull(jsonObject.getLong(PRICE_IN_CENTS_FIELD)));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject()
        .put(ID_FIELD, id.toString())
        .put(NAME_FIELD, name)
        .put(PRICE_IN_CENTS_FIELD, priceInCents);
  }
}
