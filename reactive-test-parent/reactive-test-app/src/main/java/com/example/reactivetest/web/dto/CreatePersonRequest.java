package com.example.reactivetest.web.dto;

import io.vertx.core.json.JsonObject;
import java.util.Objects;

public record CreatePersonRequest(String name) {

  public static String NAME_FIELD = "name";

  public CreatePersonRequest(JsonObject jsonObject) {
    this(Objects.requireNonNull(jsonObject.getString(NAME_FIELD)));
  }
}
