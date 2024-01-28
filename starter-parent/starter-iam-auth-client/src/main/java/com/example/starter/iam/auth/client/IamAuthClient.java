/* Licensed under Apache-2.0 2024. */
package com.example.starter.iam.auth.client;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;

import com.example.iam.auth.api.IamAuthApi;
import com.example.iam.auth.api.dto.LoginRequestDto;
import com.example.iam.auth.api.dto.LoginResponseDto;
import com.example.iam.auth.api.dto.RefreshRequestDto;
import com.example.iam.auth.api.dto.RefreshResponseDto;
import com.example.iam.auth.api.dto.RegisterRequestDto;
import com.example.iam.auth.api.dto.RegisterResponseDto;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public final class IamAuthClient implements IamAuthApi {

  private final WebClient webClient;

  @AssistedInject
  IamAuthClient(Vertx vertx, @Assisted String baseUrl, @Assisted int port) {
    this.webClient =
        WebClient.create(
            vertx, new WebClientOptions().setDefaultHost(baseUrl).setDefaultPort(port));
  }

  @Override
  public Future<LoginResponseDto> login(LoginRequestDto req) {
    return webClient
        .post("/login")
        .putHeader(CONTENT_TYPE.toString(), APPLICATION_JSON.toString())
        .sendJson(req)
        .map(h -> new LoginResponseDto(h.bodyAsJsonObject()));
  }

  @Override
  public Future<RefreshResponseDto> refresh(RefreshRequestDto req) {
    return webClient
        .post("/refresh")
        .putHeader(CONTENT_TYPE.toString(), APPLICATION_JSON.toString())
        .sendJson(req)
        .map(h -> new RefreshResponseDto(h.bodyAsJsonObject()));
  }

  @Override
  public Future<RegisterResponseDto> register(RegisterRequestDto req) {
    return webClient
        .post("/register")
        .putHeader(CONTENT_TYPE.toString(), APPLICATION_JSON.toString())
        .sendJson(req)
        .map(h -> new RegisterResponseDto(h.bodyAsJsonObject()));
  }
}
