/* Licensed under Apache-2.0 2024. */
package com.example.commons.web;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

import com.example.commons.web.serialization.JsonWriter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;

public final class ResponseWriter {

  private ResponseWriter() {}

  public static void write(RoutingContext ctx, JsonWriter jsonWriter, HttpResponseStatus status) {
    ctx.response()
        .putHeader(CONTENT_TYPE, APPLICATION_JSON)
        .setStatusCode(status.code())
        .end(jsonWriter.toJson().toBuffer())
        .onFailure(ctx::fail);
  }

  public static void writeInternalError(RoutingContext ctx) {
    ctx.response().setStatusCode(INTERNAL_SERVER_ERROR.code()).end().onFailure(ctx::fail);
  }

  public static void writeBadRequest(RoutingContext ctx) {
    ctx.response().setStatusCode(BAD_REQUEST.code()).end().onFailure(ctx::fail);
  }
}
