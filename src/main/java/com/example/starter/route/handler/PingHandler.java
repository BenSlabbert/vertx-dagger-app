package com.example.starter.route.handler;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

public class PingHandler implements Handler<RoutingContext> {

  @Override
  public void handle(RoutingContext ctx) {
    ctx.response()
        .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
        .end(Buffer.buffer("pong"))
        .onFailure(ctx::fail);
  }
}
