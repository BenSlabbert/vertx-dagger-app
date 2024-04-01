/* Licensed under Apache-2.0 2024. */
package com.example.commons.security.rpc;

import com.google.common.collect.ImmutableSet;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.serviceproxy.AuthorizationInterceptor;
import io.vertx.serviceproxy.ServiceInterceptor;
import java.util.Map;
import java.util.Set;

public class RpcServiceProxySecurityInterceptor implements ServiceInterceptor {

  private static final Logger log =
      LoggerFactory.getLogger(RpcServiceProxySecurityInterceptor.class);

  private final Map<String, SecuredAction> securedActions;

  private RpcServiceProxySecurityInterceptor(Map<String, SecuredAction> securedActions) {
    this.securedActions = securedActions;
  }

  public static ServiceInterceptor create(Map<String, SecuredAction> securedActions) {
    return new RpcServiceProxySecurityInterceptor(securedActions);
  }

  @Override
  public Future<Message<JsonObject>> intercept(
      Vertx vertx, Map<String, Object> interceptorContext, Message<JsonObject> message) {
    final ContextInternal vertxContext = (ContextInternal) vertx.getOrCreateContext();
    String action = message.headers().get("action");
    log.info("checking permissions for action: " + action);

    var securedAction = securedActions.get(action);

    if (null == securedAction) {
      // fail the client correctly
      return vertxContext.failedFuture(
          new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 403, "Forbidden"));
    }

    AuthorizationProvider ap = new RpcServiceProxyAuthorizationProvider();
    AuthorizationInterceptor authorizationInterceptor = AuthorizationInterceptor.create(ap);

    authorizationInterceptor
        .addAuthorization(RoleBasedAuthorization.create(securedAction.group()))
        .addAuthorization(RoleBasedAuthorization.create(securedAction.role()));

    securedAction.permissions().stream()
        .map(PermissionBasedAuthorization::create)
        .forEach(authorizationInterceptor::addAuthorization);

    return authorizationInterceptor.intercept(vertx, interceptorContext, message);
  }

  private static final class RpcServiceProxyAuthorizationProvider implements AuthorizationProvider {

    @Override
    public String getId() {
      return "authorization-provider";
    }

    @Override
    public void getAuthorizations(User user, Handler<AsyncResult<Void>> handler) {
      JsonObject accessToken = user.attributes().getJsonObject("accessToken");
      ACL acl = ACL.fromJson(accessToken.getJsonObject("acl"));
      String group = acl.group();
      String role = acl.role();
      Set<String> permissions = acl.permissions();
      var builder = ImmutableSet.<Authorization>builder();

      builder.add(RoleBasedAuthorization.create(group));
      builder.add(RoleBasedAuthorization.create(role));
      permissions.forEach(p -> builder.add(PermissionBasedAuthorization.create(p)));

      user.authorizations().add(getId(), builder.build());
      Future.<Void>succeededFuture().onComplete(handler);
    }
  }
}
