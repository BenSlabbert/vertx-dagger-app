/* Licensed under Apache-2.0 2023. */
package com.example.catalog.web.route.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import com.example.catalog.service.ItemService;
import com.example.catalog.web.SchemaValidatorDelegator;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import com.example.commons.web.ResponseWriter;
import github.benslabbert.vertxdaggercodegen.annotation.url.RestHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
class ItemHandlerImpl implements ItemHandler {

  private static final Logger log = LoggerFactory.getLogger(ItemHandlerImpl.class);

  private final ItemService itemService;
  private final SchemaValidatorDelegator schemaValidatorDelegator;

  @Override
  public void configureRoutes(Router router) {
    router.post(ItemHandlerImpl_ExecuteSaga_ParamParser.PATH).handler(this::executeSaga);
    router.get(ItemHandlerImpl_NextPage_ParamParser.PATH).handler(this::nextPage);
    router.get(ItemHandlerImpl_PreviousPage_ParamParser.PATH).handler(this::previousPage);
    router.get(ItemHandlerImpl_Suggest_ParamParser.PATH).handler(this::suggest);
    router.get(ItemHandlerImpl_FindOne_ParamParser.PATH).handler(this::findOne);
    router.delete(ItemHandlerImpl_DeleteOne_ParamParser.PATH).handler(this::deleteOne);
    router.post(ItemHandlerImpl_Create_ParamParser.PATH).handler(this::create);
    router.post(ItemHandlerImpl_Update_ParamParser.PATH).handler(this::update);

    log.info("Configured routes for ItemHandler");
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

  @RestHandler(path = "/execute")
  void executeSaga(RoutingContext ctx) {
    itemService
        .execute()
        .onFailure(
            err -> {
              log.error("failed to find all items", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(sagaId -> ResponseWriter.write(ctx, new JsonObject().put("sagaId", sagaId), OK));
  }

  @RestHandler(path = "/next?fromId={long:fromId=0L}&size={int:size=10}")
  void nextPage(RoutingContext ctx) {
    var params = ItemHandlerImpl_NextPage_ParamParser.parse(ctx);

    itemService
        .nextPage(params.fromId(), params.size())
        .onFailure(
            err -> {
              log.error("failed get next page", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto, OK));
  }

  @RestHandler(path = "/previous?fromId={long:fromId=0L}&size={int:size=10}")
  void previousPage(RoutingContext ctx) {
    var params = ItemHandlerImpl_PreviousPage_ParamParser.parse(ctx);

    itemService
        .previousPage(params.fromId(), params.size())
        .onFailure(
            err -> {
              log.error("failed to get previous", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto, OK));
  }

  @RestHandler(path = "/suggest?s={string:s}")
  void suggest(RoutingContext ctx) {
    var params = ItemHandlerImpl_Suggest_ParamParser.parse(ctx);

    itemService
        .suggest(params.s())
        .onFailure(
            err -> {
              log.error("failed to find suggestion: " + params.s(), err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto, OK));
  }

  @RestHandler(path = "/{long:id}")
  void findOne(RoutingContext ctx) {
    var params = ItemHandlerImpl_FindOne_ParamParser.parse(ctx);

    itemService
        .findById(params.id())
        .onFailure(
            err -> {
              log.error("failed to find item: " + params.id(), err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(
            dto -> {
              if (dto.isEmpty()) {
                ctx.response().setStatusCode(NOT_FOUND.code()).end();
                return;
              }

              ResponseWriter.write(ctx, dto.get(), OK);
            });
  }

  @RestHandler(path = "/{long:id}")
  void deleteOne(RoutingContext ctx) {
    var params = ItemHandlerImpl_DeleteOne_ParamParser.parse(ctx);

    itemService
        .delete(params.id())
        .onFailure(
            err -> {
              log.error("failed to delete item: " + params.id(), err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.writeNoContent(ctx));
  }

  @RestHandler(path = "/create")
  void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(CreateItemRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid create item request params");
      ResponseWriter.writeBadRequest(ctx);
      return;
    }

    itemService
        .create(new CreateItemRequestDto(body))
        .onFailure(
            err -> {
              log.error("failed to create item", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto, CREATED));
  }

  @RestHandler(path = "/{long:id}")
  void update(RoutingContext ctx) {
    var params = ItemHandlerImpl_Update_ParamParser.parse(ctx);

    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(UpdateItemRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid create item request params");
      ResponseWriter.writeBadRequest(ctx);
      return;
    }

    itemService
        .update(params.id(), new UpdateItemRequestDto(body))
        .onFailure(
            err -> {
              log.error("failed to create item", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.writeNoContent(ctx));
  }
}
