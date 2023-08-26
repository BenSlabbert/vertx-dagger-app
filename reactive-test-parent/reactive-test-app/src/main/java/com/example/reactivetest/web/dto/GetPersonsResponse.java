package com.example.reactivetest.web.dto;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;

public record GetPersonsResponse(List<GetPersonResponse> persons) implements JsonWriter {

  public static String PERSONS_FIELD = "persons";

  @Override
  public JsonObject toJson() {
    JsonArray array = new JsonArray();
    persons.stream().map(GetPersonResponse::toJson).forEach(array::add);
    return new JsonObject().put(PERSONS_FIELD, array);
  }
}
