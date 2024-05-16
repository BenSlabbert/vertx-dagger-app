/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.web.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaderNames.CACHE_CONTROL;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import com.example.reactivetest.config.Events;
import com.example.reactivetest.repository.sql.projection.PersonProjectionFactory.PersonProjection;
import com.example.reactivetest.service.PersonService;
import com.example.reactivetest.web.SchemaValidatorDelegator;
import com.example.reactivetest.web.dto.CreatePersonRequest;
import com.example.reactivetest.web.dto.GetPersonResponse;
import com.example.reactivetest.web.dto.GetPersonsResponse;
import com.example.reactivetest.web.dto.SseResponse;
import github.benslabbert.vertxdaggercodegen.annotation.url.RestHandler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
class PersonHandlerImpl implements PersonHandler, AutoCloseable {

  private final Vertx vertx;
  private final PersonService personService;
  private final SchemaValidatorDelegator schemaValidatorDelegator;

  private final Map<HttpServerResponse, MessageConsumer<PersonProjection>> responseConsumerMap =
      new ConcurrentHashMap<>();

  @Inject
  PersonHandlerImpl(
      Vertx vertx, PersonService personService, SchemaValidatorDelegator schemaValidatorDelegator) {
    this.vertx = vertx;
    this.personService = personService;
    this.schemaValidatorDelegator = schemaValidatorDelegator;
  }

  @Override
  public void configureRoutes(Router router) {
    router.get(PersonHandlerImpl_GetAll_ParamParser.PATH).handler(this::getAll);
    router.get(PersonHandlerImpl_Sse_ParamParser.PATH).handler(this::sse);
    router.post(PersonHandlerImpl_Create_ParamParser.PATH).handler(this::create);

    log.info("Configured routes for PersonHandler");
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

  @RestHandler(path = "/create")
  void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(CreatePersonRequest.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid create item request params");
      ctx.response().setStatusCode(BAD_REQUEST.code()).end();
      return;
    }

    CreatePersonRequest request = new CreatePersonRequest(body);
    personService
        .create(request.name())
        .onFailure(err -> ctx.end().onFailure(ctx::fail))
        .onSuccess(
            created -> {
              GetPersonResponse response = new GetPersonResponse(created.id(), created.name());
              ctx.response()
                  .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                  .setStatusCode(OK.code())
                  .end(response.toJson().toBuffer())
                  .onFailure(ctx::fail);
            });
  }

  @RestHandler(path = "/all")
  void getAll(RoutingContext ctx) {
    personService
        .findAll()
        .onFailure(err -> ctx.end().onFailure(ctx::fail))
        .onSuccess(
            projectionList -> {
              var list =
                  projectionList.stream()
                      .map(p -> new GetPersonResponse(p.id(), p.name()))
                      .toList();
              var dto = new GetPersonsResponse(list);

              ctx.response()
                  .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                  .setStatusCode(OK.code())
                  .end(dto.toJson().toBuffer())
                  .onFailure(ctx::fail);
            });
  }

  // todo move this to the service package
  @RestHandler(path = "/sse")
  void sse(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.setChunked(true);

    response.headers().add(CONTENT_TYPE, "text/event-stream;charset=UTF-8");
    response.headers().add(CONNECTION, "keep-alive");
    response.headers().add(CACHE_CONTROL, "no-cache");
    response.headers().add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");

    MessageConsumer<PersonProjection> consumer = getMessageConsumer(response);

    response.closeHandler(
        unused -> {
          log.info("response closed");
          responseConsumerMap
              .remove(response)
              .unregister()
              .onSuccess(ignore -> log.info("unregistered consumer"));
        });

    responseConsumerMap.put(response, consumer);
  }

  private MessageConsumer<PersonProjection> getMessageConsumer(HttpServerResponse response) {
    return vertx
        .eventBus()
        .consumer(
            Events.PERSON_CREATED,
            (Message<PersonProjection> msg) -> {
              var projection = msg.body();
              log.info("received event: " + projection);

              if (response.closed()) {
                log.info("response closed, not sending event");
                Optional.ofNullable(responseConsumerMap.remove(response))
                    .ifPresent(
                        c -> c.unregister().onSuccess(ignore -> log.info("unregistered consumer")));
                return;
              }

              Buffer buffer =
                  new SseResponse(projection.id(), projection.name()).toJson().toBuffer();
              response.write(buffer);
              response.write("\n");
            })
        .exceptionHandler(err -> log.error("event consumer failed", err));
  }

  @Override
  public void close() {
    responseConsumerMap.forEach(
        (k, v) -> {
          v.unregister();
          k.reset(0L);
        });
    responseConsumerMap.clear();
  }
}
