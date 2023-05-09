package com.example.iam.web.route.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static java.util.logging.Level.SEVERE;

import com.example.iam.service.UserService;
import com.example.iam.web.SchemaValidator;
import com.example.iam.web.route.dto.LoginRequestDto;
import com.example.iam.web.route.dto.RefreshRequestDto;
import com.example.iam.web.route.dto.RegisterRequestDto;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class UserHandler {

  private final UserService userService;
  private final SchemaValidator schemaValidator;

  @Inject
  public UserHandler(UserService userService, SchemaValidator schemaValidator) {
    this.userService = userService;
    this.schemaValidator = schemaValidator;
  }

  public void login(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidator.validate(LoginRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.log(SEVERE, "invalid login request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    userService
        .login(new LoginRequestDto(body))
        .onFailure(
            throwable -> {
              log.log(SEVERE, "failed to login user", throwable);
              ctx.end().onFailure(ctx::fail);
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setStatusCode(CREATED.code())
                    .end(dto.toJson().toBuffer())
                    .onFailure(ctx::fail));
  }

  public void refresh(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidator.validate(RefreshRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.log(SEVERE, "invalid refresh request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    userService
        .refresh(new RefreshRequestDto(body))
        .onFailure(
            throwable -> {
              log.log(SEVERE, "failed to refresh user", throwable);
              ctx.end().onFailure(ctx::fail);
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setStatusCode(CREATED.code())
                    .end(dto.toJson().toBuffer())
                    .onFailure(ctx::fail));
  }

  public void register(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidator.validate(RegisterRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.log(SEVERE, "invalid register request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    userService
        .register(new RegisterRequestDto(body))
        .onFailure(
            throwable -> {
              log.log(SEVERE, "failed to register user", throwable);
              ctx.end().onFailure(ctx::fail);
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setStatusCode(NO_CONTENT.code())
                    .end()
                    .onFailure(ctx::fail));
  }
}
