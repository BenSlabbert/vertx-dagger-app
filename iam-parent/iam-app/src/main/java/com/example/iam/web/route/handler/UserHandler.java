/* Licensed under Apache-2.0 2023. */
package com.example.iam.web.route.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static java.util.logging.Level.SEVERE;

import com.example.commons.web.ResponseWriter;
import com.example.iam.auth.api.IamAuthApi;
import com.example.iam.auth.api.dto.LoginRequestDto;
import com.example.iam.auth.api.dto.RefreshRequestDto;
import com.example.iam.auth.api.dto.RegisterRequestDto;
import com.example.iam.web.SchemaValidatorDelegator;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class UserHandler {

  private final IamAuthApi iamAuthApi;
  private final SchemaValidatorDelegator schemaValidatorDelegator;

  @Inject
  public UserHandler(IamAuthApi iamAuthApi, SchemaValidatorDelegator schemaValidatorDelegator) {
    this.iamAuthApi = iamAuthApi;
    this.schemaValidatorDelegator = schemaValidatorDelegator;
  }

  public void login(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(LoginRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.log(SEVERE, "invalid login request params");
      ResponseWriter.writeBadRequest(ctx);
      return;
    }

    iamAuthApi
        .login(new LoginRequestDto(body))
        .onFailure(
            err -> {
              log.log(SEVERE, "failed to login user", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto, CREATED));
  }

  public void refresh(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(RefreshRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.log(SEVERE, "invalid refresh request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    iamAuthApi
        .refresh(new RefreshRequestDto(body))
        .onFailure(
            err -> {
              log.log(SEVERE, "failed to refresh user", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto, CREATED));
  }

  public void register(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(RegisterRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.log(SEVERE, "invalid register request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    iamAuthApi
        .register(new RegisterRequestDto(body))
        .onFailure(
            err -> {
              log.log(SEVERE, "failed to register user", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto, NO_CONTENT));
  }
}
