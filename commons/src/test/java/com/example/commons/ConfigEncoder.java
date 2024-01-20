/* Licensed under Apache-2.0 2024. */
package com.example.commons;

import com.example.commons.config.Config;
import io.vertx.core.json.JsonObject;

public final class ConfigEncoder {

  private ConfigEncoder() {}

  public static JsonObject encode(Config config) {
    String encode = new JsonObject().put("empty", config).encode();
    return new JsonObject(encode).getJsonObject("empty");
  }
}
