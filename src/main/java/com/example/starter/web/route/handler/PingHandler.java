package com.example.starter.web.route.handler;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PingHandler implements Handler<RoutingContext> {

  @Inject
  public PingHandler() {
    // no args constructor required for @Inject
  }

  @Override
  public void handle(RoutingContext ctx) {
    ctx.response()
        .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
        .end(Buffer.buffer("pong"))
        .onFailure(ctx::fail);
  }
}
