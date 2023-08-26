package com.example.reactivetest.web.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static java.util.logging.Level.SEVERE;

import com.example.reactivetest.service.PersonService;
import com.example.reactivetest.web.SchemaValidatorDelegator;
import com.example.reactivetest.web.dto.CreatePersonRequest;
import com.example.reactivetest.web.dto.GetPersonResponse;
import com.example.reactivetest.web.dto.GetPersonsResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class PersonHandler {

  private final PersonService personService;
  private final SchemaValidatorDelegator schemaValidatorDelegator;

  @Inject
  public PersonHandler(
      PersonService personService, SchemaValidatorDelegator schemaValidatorDelegator) {
    this.personService = personService;
    this.schemaValidatorDelegator = schemaValidatorDelegator;
  }

  public void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(CreatePersonRequest.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.log(SEVERE, "invalid create item request params");
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

  public void getAll(RoutingContext ctx) {
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
}
