/* Licensed under Apache-2.0 2024. */
package com.example.commons.security;

import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;

import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;

public final class SecurityHandler {

  private SecurityHandler() {}

  public static void hasRole(RoutingContext ctx, Authorization authorization) {
    User user = ctx.user();

    if (null == user) {
      ctx.fail(new HttpException(UNAUTHORIZED.code()));
      return;
    }

    boolean match = authorization.match(user);

    if (!match) {
      ctx.fail(new HttpException(UNAUTHORIZED.code()));
      return;
    }

    ctx.next();
  }
}
