/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.web.dto;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;

public record GetPersonsResponse(List<GetPersonResponse> persons) {

  public static String PERSONS_FIELD = "persons";

  public JsonObject toJson() {
    JsonArray array = new JsonArray();
    persons.stream().map(GetPersonResponse::toJson).forEach(array::add);
    return new JsonObject().put(PERSONS_FIELD, array);
  }
}
