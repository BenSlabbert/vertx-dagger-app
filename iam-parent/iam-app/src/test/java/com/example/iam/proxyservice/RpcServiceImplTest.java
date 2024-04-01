/* Licensed under Apache-2.0 2024. */
package com.example.iam.proxyservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.iam.UnitTestBase;
import com.example.iam.entity.ACL;
import com.example.iam.service.TokenService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.AuthenticationInterceptor;
import io.vertx.serviceproxy.AuthorizationInterceptor;
import io.vertx.serviceproxy.impl.InterceptorHolder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class RpcServiceImplTest extends UnitTestBase {

  private String getToken() {
    TokenService tokenService = provider.tokenService();
    return tokenService.authToken(
        "user", ACL.builder().group("group").role("role").permissions(Set.of("p1", "p2")).build());
  }

  @Test
  void test(Vertx vertx, VertxTestContext testContext) {
    vertx.exceptionHandler(err -> System.err.println("unhandled exception: " + err));

    vertx
        .eventBus()
        .addInboundInterceptor(
            ctx -> {
              System.err.println("inbound interceptor");
              ctx.next();
            })
        .addOutboundInterceptor(
            ctx -> {
              System.err.println("outbound interceptor");
              ctx.next();
            });

    var authenticationInterceptor =
        new InterceptorHolder(
            AuthenticationInterceptor.create(
                new AuthenticationProvider() {
                  @Override
                  public void authenticate(
                      JsonObject jsonObject, Handler<AsyncResult<User>> handler) {
                    throw new UnsupportedOperationException("deprecated method not implemented");
                  }

                  @Override
                  public Future<User> authenticate(Credentials credentials) {
                    // request must have a header called "auth-token" which is a JWT token
                    if (null == credentials) {
                      return Future.failedFuture("credentials required");
                    }

                    String jwtToken = credentials.toJson().getString("token");

                    if (StringUtils.isBlank(jwtToken)) {
                      return Future.failedFuture("JWT credentials required");
                    }

                    TokenService tokenService = provider.tokenService();
                    return tokenService.authenticate(jwtToken);
                  }
                }));

    InterceptorHolder authenticatedUserInterceptor =
        new InterceptorHolder(
            (_vertx, interceptorContext, message) -> {
              final ContextInternal vertxContext = (ContextInternal) _vertx.getOrCreateContext();
              User user = (User) interceptorContext.get("user");
              // do something with the authenticated user
              System.err.println(user.subject() + ": accessing service proxy");
              return vertxContext.succeededFuture(message);
            });

    AuthorizationInterceptor authorizationInterceptor =
        AuthorizationInterceptor.create(
            new AuthorizationProvider() {
              @Override
              public String getId() {
                return "custom-auth-provider";
              }

              @Override
              public void getAuthorizations(User user, Handler<AsyncResult<Void>> handler) {
                JsonObject accessToken = user.attributes().getJsonObject("accessToken");
                // this ACL object needs to be part of the iam-api library
                ACL acl = new ACL(accessToken.getJsonObject("acl"));
                String group = acl.group();
                String role = acl.role();
                Set<String> permissions = acl.permissions();

                Set<Authorization> authorizations = new HashSet<>(permissions.size() + 2);

                authorizations.add(RoleBasedAuthorization.create(group));
                authorizations.add(RoleBasedAuthorization.create(role));
                permissions.forEach(
                    p -> authorizations.add(PermissionBasedAuthorization.create(p)));

                user.authorizations().add(getId(), authorizations);
                Future.<Void>succeededFuture().onComplete(handler);
              }
            });

    // check required roles and permissions
    // this will run for all methods of the service
    // if wish to provide per method authorization, implement a ServiceInterceptor
    // new InterceptorHolder(ServiceInterceptor)
    // and add it to the list of interceptors
    var authInterceptor =
        new InterceptorHolder(
            authorizationInterceptor
                .addAuthorization(RoleBasedAuthorization.create("group"))
                .addAuthorization(RoleBasedAuthorization.create("role"))
                .addAuthorization(PermissionBasedAuthorization.create("p1"))
                .addAuthorization(PermissionBasedAuthorization.create("p2")));

    InterceptorHolder perMethodAuth =
        new InterceptorHolder(
            (_vertx, interceptorContext, message) -> {
              final ContextInternal vertxContext = (ContextInternal) _vertx.getOrCreateContext();
              String action = message.headers().get("action");
              System.err.println("checking permissions for action: " + action);

              // authInterceptor above apples to all methods
              // here we can apply per method authorization
              if ("getProtectedString".equals(action)) {
                System.err.println("check for additional permissions");
                return vertxContext.succeededFuture(message);
              }

              // fail the client correctly
              return vertxContext.failedFuture(
                  new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 403, "Forbidden"));
            });

    MessageConsumer<JsonObject> consumer =
        new RpcServiceVertxProxyHandler(vertx, new RpcServiceImpl())
            .register(
                vertx,
                RpcService.ADDRESS,
                List.of(
                    authenticationInterceptor,
                    authenticatedUserInterceptor,
                    perMethodAuth,
                    authInterceptor))
            .setMaxBufferedMessages(100)
            .fetch(10)
            .exceptionHandler(err -> System.err.println("exception in event bus: " + err))
            .endHandler(ignore -> System.err.println("end handler"));

    RpcServiceVertxEBProxy proxy =
        new RpcServiceVertxEBProxy(
            vertx, RpcService.ADDRESS, new DeliveryOptions().addHeader("auth-token", getToken()));
    Future<String> admin = proxy.getProtectedString("admin");

    admin.onComplete(
        testContext.succeeding(
            res ->
                testContext.verify(
                    () -> {
                      assertThat(res).isEqualTo("admin");
                      consumer.unregister().onComplete(ignore -> testContext.completeNow());
                    })));
  }
}
