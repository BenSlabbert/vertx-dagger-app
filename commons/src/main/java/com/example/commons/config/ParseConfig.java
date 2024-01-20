/* Licensed under Apache-2.0 2023. */
package com.example.commons.config;

import io.vertx.core.json.JsonObject;
import lombok.extern.java.Log;

@Log
public class ParseConfig {

  private ParseConfig() {}

  public static Config get(JsonObject jsonObject) {
    return Config.fromJson(jsonObject);
  }
}
