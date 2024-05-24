/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

import static io.vertx.json.schema.common.dsl.Keywords.minLength;
import static io.vertx.json.schema.common.dsl.Schemas.arraySchema;
import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.auto.value.extension.toprettystring.ToPrettyString;
import com.google.common.collect.ImmutableSet;
import github.benslabbert.vertxdaggercommons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;
import java.util.Set;

@AutoValue
public abstract class Access implements JsonWriter {

  private static final String GROUP_FIELD = "group";
  private static final String ROLE_FIELD = "role";
  private static final String PERMISSIONS_FIELD = "permissions";

  public static final ObjectSchemaBuilder SCHEMA =
      objectSchema()
          .requiredProperty(GROUP_FIELD, stringSchema().with(minLength(1)))
          .requiredProperty(ROLE_FIELD, stringSchema().with(minLength(1)))
          .requiredProperty(
              PERMISSIONS_FIELD, arraySchema().items(stringSchema().with(minLength(1))));

  public abstract String group();

  public abstract String role();

  public abstract Set<String> permissions();

  @Override
  @Memoized
  public abstract int hashCode();

  @ToPrettyString
  public abstract String toPrettyString();

  @Override
  @Memoized
  public abstract String toString();

  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();

    json.put(GROUP_FIELD, group());
    json.put(ROLE_FIELD, role());

    JsonArray array = new JsonArray();
    permissions().forEach(array::add);
    json.put(PERMISSIONS_FIELD, array);

    return json;
  }

  public static Builder builder() {
    return new AutoValue_Access.Builder();
  }

  public static Access fromJson(JsonObject jsonObject) {
    Builder builder = builder();

    String group = jsonObject.getString(GROUP_FIELD);
    builder.group(group);

    String role = jsonObject.getString(ROLE_FIELD);
    builder.role(role);

    jsonObject.getJsonArray(PERMISSIONS_FIELD).stream()
        .map(Object::toString)
        .forEach(builder::addPermission);

    return builder.build();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder group(String group);

    public abstract Builder role(String role);

    abstract ImmutableSet.Builder<String> permissionsBuilder();

    public final Builder addPermission(String permission) {
      permissionsBuilder().add(permission);
      return this;
    }

    public final Builder addPermissions(String... permissions) {
      permissionsBuilder().add(permissions);
      return this;
    }

    abstract Access autoBuild();

    public final Access build() {
      Access access = autoBuild();
      if (access.permissions().isEmpty()) {
        throw new IllegalStateException("must provide at least one permission");
      }
      return access;
    }
  }
}
