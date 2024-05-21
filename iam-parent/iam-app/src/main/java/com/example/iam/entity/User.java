/* Licensed under Apache-2.0 2023. */
package com.example.iam.entity;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record User(String username, String password, String refreshToken, ACL acl) {

  public static final String USERNAME_FIELD = "username";
  public static final String PASSWORD_FIELD = "password";
  public static final String REFRESH_TOKEN_FIELD = "refreshToken";
  public static final String ACl_FIELD = "acl";

  public static Builder builder() {
    return new AutoBuilder_User_Builder();
  }

  public static User fromJson(JsonObject json) {
    return User_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return User_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder username(String username);

    Builder password(String password);

    Builder refreshToken(String refreshToken);

    Builder acl(ACL acl);

    User build();
  }
}
