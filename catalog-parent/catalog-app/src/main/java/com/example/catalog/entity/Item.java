package com.example.catalog.entity;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.UUID;

public record Item(UUID id, String name, long priceInCents) implements JsonWriter {

  public static String ID_FIELD = "id";
  public static String NAME_FIELD = "name";
  public static String PRICE_IN_CENTS_FIELD = "priceInCents";

  @Override
  public JsonObject toJson() {
    return new JsonObject()
        .put(ID_FIELD, id.toString())
        .put(NAME_FIELD, name)
        .put(PRICE_IN_CENTS_FIELD, priceInCents);
  }
}
