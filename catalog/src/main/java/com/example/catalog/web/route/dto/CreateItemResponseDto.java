package com.example.catalog.web.route.dto;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import java.util.UUID;

public record CreateItemResponseDto(long id, UUID uuid, String name, long priceInCents)
    implements JsonWriter {

  public static String ID_FIELD = "id";
  public static String UUID_FIELD = "uuid";
  public static String NAME_FIELD = "name";
  public static String PRICE_IN_CENTS_FIELD = "priceInCents";

  public CreateItemResponseDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getLong(ID_FIELD)),
        UUID.fromString(Objects.requireNonNull(jsonObject.getString(UUID_FIELD))),
        Objects.requireNonNull(jsonObject.getString(NAME_FIELD)),
        Objects.requireNonNull(jsonObject.getLong(PRICE_IN_CENTS_FIELD)));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject()
        .put(ID_FIELD, id)
        .put(UUID_FIELD, uuid)
        .put(NAME_FIELD, name)
        .put(PRICE_IN_CENTS_FIELD, priceInCents);
  }
}
