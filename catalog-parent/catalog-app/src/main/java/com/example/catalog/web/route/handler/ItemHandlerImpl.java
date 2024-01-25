/* Licensed under Apache-2.0 2023. */
package com.example.catalog.web.route.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import com.example.catalog.service.ItemService;
import com.example.catalog.web.SchemaValidatorDelegator;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
class ItemHandlerImpl implements ItemHandler {

  private static final Logger log = LoggerFactory.getLogger(ItemHandlerImpl.class);

  private final ItemService itemService;
  private final SchemaValidatorDelegator schemaValidatorDelegator;

  @Override
  public void executeSaga(RoutingContext ctx) {
    itemService
        .execute()
        .onFailure(
            err -> {
              log.error("failed to find all items", err);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
            })
        .onSuccess(
            sagaId ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setStatusCode(OK.code())
                    .end(new JsonObject().put("sagaId", sagaId).toBuffer())
                    .onFailure(ctx::fail));
  }

  @Override
  public void nextPage(RoutingContext ctx, long fromId, int size) {
    itemService
        .nextPage(fromId, size)
        .onFailure(
            err -> {
              log.error("failed to find all items", err);
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

  @Override
  public void previousPage(RoutingContext ctx, long fromId, int size) {
    itemService
        .previousPage(fromId, size)
        .onFailure(
            err -> {
              log.error("failed to find all items", err);
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

  @Override
  public void suggest(RoutingContext ctx, String name) {
    itemService
        .suggest(name)
        .onFailure(
            err -> {
              log.error("failed to find suggestion: " + name, err);
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

  @Override
  public void findOne(RoutingContext ctx, long id) {
    itemService
        .findById(id)
        .onFailure(
            err -> {
              log.error("failed to find item: " + id, err);
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

  @Override
  public void deleteOne(RoutingContext ctx, long id) {
    itemService
        .delete(id)
        .onFailure(
            err -> {
              log.error("failed to delete item: " + id, err);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
            })
        .onSuccess(
            dto ->
                ctx.response()
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setStatusCode(NO_CONTENT.code())
                    .end()
                    .onFailure(ctx::fail));
  }

  @Override
  public void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(CreateItemRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid create item request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    itemService
        .create(new CreateItemRequestDto(body))
        .onFailure(
            err -> {
              log.error("failed to create item", err);
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

  @Override
  public void update(RoutingContext ctx, long id) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(UpdateItemRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid create item request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    itemService
        .update(id, new UpdateItemRequestDto(body))
        .onFailure(
            err -> {
              log.error("failed to create item", err);
              ctx.fail(new HttpException(INTERNAL_SERVER_ERROR.code()));
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
