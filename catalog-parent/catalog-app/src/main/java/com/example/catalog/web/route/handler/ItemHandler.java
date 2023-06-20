package com.example.catalog.web.route.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static java.util.logging.Level.SEVERE;

import com.example.catalog.service.ItemService;
import com.example.catalog.web.SchemaValidatorDelegator;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import java.util.UUID;
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

  public void findAll(RoutingContext ctx, Integer from, Integer to) {
    itemService
        .findAll(from, to)
        .onFailure(
            err -> {
              log.log(SEVERE, "failed to find all items", err);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setStatusCode(OK.code())
                    .end(dto.toJson().toBuffer())
                    .onFailure(ctx::fail));
  }

  public void search(
      RoutingContext ctx,
      String searchQuery,
      Integer priceFrom,
      Integer priceTo,
      Integer from,
      Integer to) {

    itemService
        .search(searchQuery, priceFrom, priceTo, from, to)
        .onFailure(
            err -> {
              log.log(SEVERE, "failed to search for items", err);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setStatusCode(OK.code())
                    .end(dto.toJson().toBuffer())
                    .onFailure(ctx::fail));
  }

  public void findOne(RoutingContext ctx, UUID id) {
    itemService
        .findById(id)
        .onFailure(
            err -> {
              log.log(SEVERE, "failed to find item: " + id, err);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
            })
        .onSuccess(
            dto -> {
              if (dto.isEmpty()) {
                ctx.response().setStatusCode(NOT_FOUND.code()).end();
                return;
              }

              ctx.response()
                  .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                  .setStatusCode(OK.code())
                  .end(dto.get().toJson().toBuffer())
                  .onFailure(ctx::fail);
            });
  }

  public void deleteOne(RoutingContext ctx, UUID id) {
    itemService
        .delete(id)
        .onFailure(
            err -> {
              log.log(SEVERE, "failed to delete item: " + id, err);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
            })
        .onSuccess(
            dto -> {
              if (dto.isEmpty()) {
                ctx.response().setStatusCode(NOT_FOUND.code()).end();
                return;
              }

              ctx.response()
                  .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                  .setStatusCode(NO_CONTENT.code())
                  .end(dto.get().toJson().toBuffer())
                  .onFailure(ctx::fail);
            });
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
            err -> {
              log.log(SEVERE, "failed to create item", err);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setStatusCode(CREATED.code())
                    .end(dto.toJson().toBuffer())
                    .onFailure(ctx::fail));
  }

  public void update(RoutingContext ctx, UUID id) {
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
            err -> {
              log.log(SEVERE, "failed to create item", err);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
            })
        .onSuccess(
            dto -> {
              if (dto.isEmpty()) {
                ctx.response().setStatusCode(NOT_FOUND.code()).end();
                return;
              }

              ctx.response()
                  .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                  .setStatusCode(NO_CONTENT.code())
                  .end(dto.get().toJson().toBuffer())
                  .onFailure(ctx::fail);
            });
  }
}
