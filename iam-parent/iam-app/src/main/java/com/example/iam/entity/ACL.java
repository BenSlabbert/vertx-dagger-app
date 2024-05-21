/* Licensed under Apache-2.0 2024. */
package com.example.iam.entity;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.Set;

@JsonWriter
public record ACL(String group, String role, Set<String> permissions) {

  public static final String GROUP_FIELD = "group";
  public static final String ROLE_FIELD = "role";
  public static final String PERMISSIONS_FIELD = "permissions";

  public static Builder builder() {
    return new AutoBuilder_ACL_Builder();
  }

  public static ACL fromJson(JsonObject json) {
    return ACL_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return ACL_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder group(String group);

    Builder role(String role);

    Builder permissions(Set<String> permissions);

    ACL build();
  }
}
