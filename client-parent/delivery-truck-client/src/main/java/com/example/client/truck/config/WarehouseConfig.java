/* Licensed under Apache-2.0 2024. */
package com.example.client.truck.config;

import com.google.auto.value.AutoBuilder;
import io.vertx.core.json.JsonObject;

public record WarehouseConfig(String host) {

  public static WarehouseConfig fromJson(JsonObject jsonObject) {
    JsonObject config = jsonObject.getJsonObject("warehouseConfig", new JsonObject());

    return new AutoBuilder_WarehouseConfig_Builder().host(config.getString("host")).build();
  }

  @AutoBuilder
  interface Builder {

    Builder host(String host);

    WarehouseConfig build();
  }
}
