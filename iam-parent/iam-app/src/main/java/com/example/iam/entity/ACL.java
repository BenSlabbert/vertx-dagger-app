/* Licensed under Apache-2.0 2024. */
package com.example.iam.entity;

import static java.util.Objects.requireNonNull;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public record ACL(String group, String role, Set<String> permissions) implements JsonWriter {

  public static final String GROUP_FIELD = "group";
  public static final String ROLE_FIELD = "role";
  public static final String PERMISSIONS_FIELD = "permissions";

  public ACL(JsonObject jsonObject) {
    this(
        requireNonNull(jsonObject.getString(GROUP_FIELD)),
        requireNonNull(jsonObject.getString(ROLE_FIELD)),
        requireNonNull(
            jsonObject.getJsonArray(PERMISSIONS_FIELD).stream()
                .map(Object::toString)
                .collect(Collectors.toSet())));
  }

  @Override
  public JsonObject toJson() {
    JsonArray array = new JsonArray();
    permissions.forEach(array::add);
    return new JsonObject()
        .put(GROUP_FIELD, group)
        .put(ROLE_FIELD, role)
        .put(PERMISSIONS_FIELD, array);
  }
}
