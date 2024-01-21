/* Licensed under Apache-2.0 2023. */
package com.example.catalog.web.route.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;

import com.example.iam.rpc.api.AuthenticationIntegration;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthHandler implements Handler<RoutingContext> {

  public static final RoleBasedAuthorization ROLE = RoleBasedAuthorization.create("my-role");

  private static final Logger log = LoggerFactory.getLogger(AuthHandler.class);
  private static final String BEARER = "Bearer ";

  private final AuthenticationIntegration authenticationIntegration;

  @Inject
  AuthHandler(AuthenticationIntegration authenticationIntegration) {
    this.authenticationIntegration = authenticationIntegration;
  }

  @Override
  public void handle(RoutingContext ctx) {
    String authHeader = ctx.request().getHeader(HttpHeaders.AUTHORIZATION);
    // todo: wtf???
    //  if we do not have this we cannot re-read the request later
    //  was not needed for tests, but debugging with cURL helped
    ctx.request().pause();

    if (null == authHeader) {
      log.warn("invalid header: auth header is null");
      ctx.fail(new HttpException(UNAUTHORIZED.code()));
      return;
    }

    if (!authHeader.startsWith(BEARER)) {
      log.warn("invalid header: auth header incorrect prefix");
      ctx.fail(new HttpException(UNAUTHORIZED.code()));
      return;
    }

    String token = authHeader.substring(BEARER.length());
    authenticationIntegration
        .isTokenValid(token)
        .onFailure(
            err -> {
              log.error("iam call failed", err);
              ctx.fail(new HttpException(UNAUTHORIZED.code()));
            })
        .onSuccess(
            resp -> {
              log.info("token valid? " + resp.isValid());
              if (!resp.isValid()) {
                ctx.fail(new HttpException(UNAUTHORIZED.code()));
                return;
              }

              JsonObject principal = new JsonObject(resp.getUserPrincipal());
              JsonObject attributes = new JsonObject(resp.getUserAttributes());

              log.info("principal: " + principal);
              log.info("attributes: " + attributes);

              // todo: read the principal and attributes
              //  to determine the proper roles
              User user = User.create(principal, attributes);
              user.authorizations().add("role-provider-id", ROLE);

              ctx.setUser(user);
              ctx.next();
            });
  }
}
