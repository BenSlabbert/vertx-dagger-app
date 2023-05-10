package com.example.iam.web.route.dto;

import io.vertx.core.json.JsonObject;

public sealed interface JsonWriter
    permits LoginRequestDto,
        LoginResponseDto,
        RefreshRequestDto,
        RefreshResponseDto,
        RegisterRequestDto,
        RegisterResponseDto {

  JsonObject toJson();
}
