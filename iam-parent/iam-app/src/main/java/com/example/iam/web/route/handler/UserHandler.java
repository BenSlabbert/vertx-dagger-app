/* Licensed under Apache-2.0 2023. */
package com.example.iam.web.route.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;

import com.example.commons.web.ResponseWriter;
import com.example.iam.auth.api.IamAuthApi;
import com.example.iam.auth.api.dto.LoginRequestDto;
import com.example.iam.auth.api.dto.RefreshRequestDto;
import com.example.iam.auth.api.dto.RegisterRequestDto;
import com.example.iam.auth.api.dto.UpdatePermissionsRequestDto;
import com.example.iam.web.SchemaValidatorDelegator;
import github.benslabbert.vertxdaggercodegen.annotation.url.RestHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class UserHandler {

  private static final Logger log = LoggerFactory.getLogger(UserHandler.class);

  private final IamAuthApi iamAuthApi;
  private final SchemaValidatorDelegator schemaValidatorDelegator;

  @Inject
  UserHandler(IamAuthApi iamAuthApi, SchemaValidatorDelegator schemaValidatorDelegator) {
    this.iamAuthApi = iamAuthApi;
    this.schemaValidatorDelegator = schemaValidatorDelegator;
  }

  public void configureRoutes(Router router) {
    router.post(UserHandler_Login_ParamParser.PATH).handler(this::login);
    router.post(UserHandler_Refresh_ParamParser.PATH).handler(this::refresh);
    router.post(UserHandler_Register_ParamParser.PATH).handler(this::register);
    router.post(UserHandler_UpdatePermissions_ParamParser.PATH).handler(this::updatePermissions);

    log.info("Configured routes for UserHandler");
    log.info("-------------------------");
    router
        .getRoutes()
        .forEach(
            route -> {
              log.info("Path: " + route.getPath());
              log.info("Methods: " + route.methods());
              log.info("-------------------------");
            });
  }

  @RestHandler(path = "/login")
  void login(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(LoginRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid login request params");
      ResponseWriter.writeBadRequest(ctx);
      return;
    }

    iamAuthApi
        .login(new LoginRequestDto(body))
        .onFailure(
            err -> {
              log.error("failed to login user", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto, CREATED));
  }

  @RestHandler(path = "/refresh")
  void refresh(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(RefreshRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid refresh request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    iamAuthApi
        .refresh(new RefreshRequestDto(body))
        .onFailure(
            err -> {
              log.error("failed to refresh user", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto, CREATED));
  }

  @RestHandler(path = "/register")
  void register(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(RegisterRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid register request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    iamAuthApi
        .register(new RegisterRequestDto(body))
        .onFailure(
            err -> {
              log.error("failed to register user", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto, NO_CONTENT));
  }

  @RestHandler(path = "/update-permissions")
  void updatePermissions(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(UpdatePermissionsRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid register request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    iamAuthApi
        .updatePermissions(new UpdatePermissionsRequestDto(body))
        .onFailure(
            err -> {
              log.error("failed to update user permissions", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto, NO_CONTENT));
  }
}
