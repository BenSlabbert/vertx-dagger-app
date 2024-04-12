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
import com.example.iam.auth.api.dto.UpdatePermissionsRequestDto;
import com.example.iam.auth.api.dto.UpdatePermissionsResponseDto;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpStatusClass;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.HttpException;
import java.util.function.Function;

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
        .post("/api/login")
        .putHeader(CONTENT_TYPE.toString(), APPLICATION_JSON.toString())
        .sendJson(req.toJson())
        .compose(resp -> handleResponse(resp, LoginResponseDto::new));
  }

  @Override
  public Future<RefreshResponseDto> refresh(RefreshRequestDto req) {
    return webClient
        .post("/api/refresh")
        .putHeader(CONTENT_TYPE.toString(), APPLICATION_JSON.toString())
        .sendJson(req.toJson())
        .compose(resp -> handleResponse(resp, RefreshResponseDto::new));
  }

  @Override
  public Future<RegisterResponseDto> register(RegisterRequestDto req) {
    return webClient
        .post("/api/register")
        .putHeader(CONTENT_TYPE.toString(), APPLICATION_JSON.toString())
        .sendJson(req.toJson())
        .compose(resp -> handleResponse(resp, RegisterResponseDto::new));
  }

  @Override
  public Future<UpdatePermissionsResponseDto> updatePermissions(UpdatePermissionsRequestDto req) {
    return webClient
        .post("/api/update-permissions")
        .putHeader(CONTENT_TYPE.toString(), APPLICATION_JSON.toString())
        .sendJson(req.toJson())
        .compose(resp -> handleResponse(resp, UpdatePermissionsResponseDto::new));
  }

  private <T> Future<T> handleResponse(
      HttpResponse<Buffer> resp, Function<JsonObject, T> function) {
    var status = HttpResponseStatus.valueOf(resp.statusCode());
    if (status.codeClass() == HttpStatusClass.SUCCESS) {
      return Future.succeededFuture(function.apply(resp.bodyAsJsonObject()));
    } else {
      return Future.failedFuture(new HttpException(resp.statusCode()));
    }
  }
}
