package com.example.starter.config;

import io.vertx.core.json.JsonObject;
import lombok.Builder;

@Builder
public record Config(
    HttpConfig httpConfig, RedisConfig redisConfig, VerticleConfig verticleConfig) {

  public static Config defaults() {
    return Config.builder()
        .httpConfig(HttpConfig.builder().port(8080).build())
        .redisConfig(RedisConfig.builder().host("localhost").port(6379).database(0).build())
        .build();
  }

  public static Config fromJson(JsonObject jsonObject) {
    JsonObject httpConfig = jsonObject.getJsonObject("httpConfig");
    JsonObject redisConfig = jsonObject.getJsonObject("redisConfig");
    JsonObject verticleConfig = jsonObject.getJsonObject("verticleConfig", new JsonObject());

    return Config.builder()
        .httpConfig(HttpConfig.builder().port(httpConfig.getInteger("port")).build())
        .redisConfig(
            RedisConfig.builder()
                .host(redisConfig.getString("host"))
                .port(redisConfig.getInteger("port"))
                .database(redisConfig.getInteger("database"))
                .build())
        .verticleConfig(
            VerticleConfig.builder()
                .numberOfInstances(verticleConfig.getInteger("numberOfInstances", 1))
                .build())
        .build();
  }

  @Builder
  public record VerticleConfig(int numberOfInstances) {}

  @Builder
  public record HttpConfig(int port) {}

  @Builder
  public record RedisConfig(String host, int port, int database) {

    public String uri() {
      return String.format("redis://%s:%d/%d", host, port, database);
    }
  }
}
