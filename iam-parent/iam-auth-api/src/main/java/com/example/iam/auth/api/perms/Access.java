/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

import com.example.commons.web.serialization.JsonWriter;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.auto.value.extension.toprettystring.ToPrettyString;
import com.google.common.collect.ImmutableSet;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Set;

@AutoValue
public abstract class Access implements JsonWriter {

  public abstract Group group();

  public abstract Role role();

  public abstract Set<Permission> permissions();

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

    json.put("group", group().name());
    json.put("role", role().name());

    JsonArray array = new JsonArray();
    permissions().stream().map(Permission::name).forEach(array::add);
    json.put("permissions", array);

    return json;
  }

  public static Builder builder() {
    return new AutoValue_Access.Builder();
  }

  public static Access fromJson(JsonObject jsonObject) {
    Builder builder = builder();

    String group = jsonObject.getString("group");
    builder.group(Group.groupForName().get(group));

    String role = jsonObject.getString("role");
    builder.role(Role.roleForName().get(role));

    jsonObject.getJsonArray("permissions").stream()
        .map(Object::toString)
        .forEach(p -> builder.addPermission(Permission.permissionForName().get(p)));

    return builder.build();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder group(Group group);

    public abstract Builder role(Role role);

    abstract ImmutableSet.Builder<Permission> permissionsBuilder();

    public final Builder addPermission(Permission permission) {
      permissionsBuilder().add(permission);
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
