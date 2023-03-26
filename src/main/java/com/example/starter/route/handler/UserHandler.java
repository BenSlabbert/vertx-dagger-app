package com.example.starter.route.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;

import com.example.starter.route.handler.dto.LoginRequestDto;
import com.example.starter.route.handler.dto.RefreshRequestDto;
import com.example.starter.route.handler.dto.RegisterRequestDto;
import com.example.starter.service.UserService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class UserHandler {

  private final UserService userService;

  @Inject
  public UserHandler(UserService userService) {
    this.userService = userService;
  }

  public void login(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();

    userService
        .login(new LoginRequestDto(body))
        .onFailure(
            throwable -> {
              log.info(throwable.toString());
              ctx.end().onFailure(ctx::fail);
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(dto.toJson().toBuffer())
                    .onFailure(ctx::fail));
  }

  public void refresh(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();

    userService
        .refresh(new RefreshRequestDto(body))
        .onFailure(
            throwable -> {
              log.info(throwable.toString());
              ctx.end().onFailure(ctx::fail);
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(dto.toJson().toBuffer())
                    .onFailure(ctx::fail));
  }

  public void register(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();

    userService
        .register(new RegisterRequestDto(body))
        .onFailure(
            throwable -> {
              log.info(throwable.toString());
              ctx.end().onFailure(ctx::fail);
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(dto.toJson().toBuffer())
                    .onFailure(ctx::fail));
  }
}
