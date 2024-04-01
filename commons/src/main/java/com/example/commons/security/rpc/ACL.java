/* Licensed under Apache-2.0 2024. */
package com.example.commons.security.rpc;

import static java.util.Objects.requireNonNull;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Set;
import java.util.stream.Collectors;

@AutoValue
public abstract class ACL {

  private static final String GROUP_FIELD = "group";
  private static final String ROLE_FIELD = "role";
  private static final String PERMISSIONS_FIELD = "permissions";

  public abstract String group();

  public abstract String role();

  public abstract Set<String> permissions();

  ACL() {}

  public static ACL fromJson(JsonObject jsonObject) {
    return new JwtAcl(
        requireNonNull(jsonObject.getString(GROUP_FIELD)),
        requireNonNull(jsonObject.getString(ROLE_FIELD)),
        requireNonNull(
            jsonObject.getJsonArray(PERMISSIONS_FIELD).stream()
                .map(Object::toString)
                .collect(Collectors.toSet())));
  }

  public JsonObject toJson() {
    JsonArray array = new JsonArray();
    permissions().forEach(array::add);
    return new JsonObject()
        .put(GROUP_FIELD, group())
        .put(ROLE_FIELD, role())
        .put(PERMISSIONS_FIELD, array);
  }

  public static Builder builder() {
    return new AutoValue_ACL.Builder();
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

    public abstract ACL build();
  }

  private static final class JwtAcl extends ACL {

    private final String group;
    private final String role;
    private final Set<String> permissions;

    private JwtAcl(String group, String role, Set<String> permissions) {
      this.group = group;
      this.role = role;
      this.permissions = permissions;
    }

    @Override
    public String group() {
      return group;
    }

    @Override
    public String role() {
      return role;
    }

    @Override
    public Set<String> permissions() {
      return permissions;
    }
  }
}
