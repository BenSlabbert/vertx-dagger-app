/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.config;

import com.google.auto.value.AutoBuilder;
import io.vertx.core.json.JsonObject;

public record IamConfig(String host, int port, String username, String password) {

  public static IamConfig fromJson(JsonObject jsonObject) {
    JsonObject config = jsonObject.getJsonObject("iamConfig", new JsonObject());

    return new AutoBuilder_IamConfig_Builder()
        .host(config.getString("host"))
        .port(config.getInteger("port"))
        .username(config.getString("username"))
        .password(config.getString("password"))
        .build();
  }

  @AutoBuilder
  interface Builder {

    Builder host(String host);

    Builder port(int port);

    Builder username(String username);

    Builder password(String password);

    IamConfig build();
  }
}
