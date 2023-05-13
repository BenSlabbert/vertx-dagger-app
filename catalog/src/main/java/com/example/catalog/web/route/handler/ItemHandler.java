package com.example.catalog.web.route.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static java.util.logging.Level.SEVERE;

import com.example.catalog.service.ItemService;
import com.example.catalog.web.SchemaValidatorDelegator;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.FindAllRequestDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class ItemHandler {

  private final ItemService itemService;
  private final SchemaValidatorDelegator schemaValidatorDelegator;

  @Inject
  public ItemHandler(ItemService itemService, SchemaValidatorDelegator schemaValidatorDelegator) {
    this.itemService = itemService;
    this.schemaValidatorDelegator = schemaValidatorDelegator;
  }

  public void findAll(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(CreateItemRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.log(SEVERE, "invalid create item request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    itemService
        .findAll(new FindAllRequestDto(body))
        .onFailure(
            throwable -> {
              log.log(SEVERE, "failed to find all items", throwable);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
              ctx.end();
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setStatusCode(CREATED.code())
                    .end(dto.toJson().toBuffer())
                    .onFailure(ctx::fail));
  }

  public void findOne(RoutingContext ctx, long id) {
    itemService
        .findById(id)
        .onFailure(
            throwable -> {
              log.log(SEVERE, "failed to find all items", throwable);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
              ctx.end();
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setStatusCode(CREATED.code())
                    .end(dto.toJson().toBuffer())
                    .onFailure(ctx::fail));
  }

  public void deleteOne(RoutingContext ctx, long id) {
    itemService
        .delete(id)
        .onFailure(
            throwable -> {
              log.log(SEVERE, "failed to find all items", throwable);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
              ctx.end();
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setStatusCode(CREATED.code())
                    .end(dto.toJson().toBuffer())
                    .onFailure(ctx::fail));
  }

  public void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(CreateItemRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.log(SEVERE, "invalid create item request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    itemService
        .create(new CreateItemRequestDto(body))
        .onFailure(
            throwable -> {
              log.log(SEVERE, "failed to create item", throwable);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
              ctx.end();
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setStatusCode(CREATED.code())
                    .end(dto.toJson().toBuffer())
                    .onFailure(ctx::fail));
  }

  public void update(RoutingContext ctx, long id) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(UpdateItemRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.log(SEVERE, "invalid create item request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    itemService
        .update(id, new UpdateItemRequestDto(body))
        .onFailure(
            throwable -> {
              log.log(SEVERE, "failed to create item", throwable);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
              ctx.end();
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setStatusCode(CREATED.code())
                    .end(dto.toJson().toBuffer())
                    .onFailure(ctx::fail));
  }
}
