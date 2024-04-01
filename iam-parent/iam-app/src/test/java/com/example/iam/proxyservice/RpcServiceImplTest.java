/* Licensed under Apache-2.0 2024. */
package com.example.iam.proxyservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.commons.rpc.UserAccessLoggerInterceptor;
import com.example.commons.security.rpc.RpcServiceProxySecurityInterceptor;
import com.example.iam.UnitTestBase;
import com.example.iam.entity.ACL;
import com.example.iam.service.TokenService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.AuthenticationInterceptor;
import io.vertx.serviceproxy.ServiceInterceptor;
import io.vertx.serviceproxy.impl.InterceptorHolder;
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

                    // in a "real" service we will call the iam service to validate the token
                    // and return the User object back to use
                    TokenService tokenService = provider.tokenService();
                    return tokenService.authenticate(jwtToken);
                  }
                }));

    InterceptorHolder accessLogger = new InterceptorHolder(UserAccessLoggerInterceptor.create());

    ServiceInterceptor serviceInterceptor =
        RpcServiceProxySecurityInterceptor.create(RpcService_SecuredActions.getSecuredActions());
    InterceptorHolder interceptorHolder = new InterceptorHolder(serviceInterceptor);

    MessageConsumer<JsonObject> consumer =
        new RpcServiceVertxProxyHandler(vertx, new RpcServiceImpl())
            .register(
                vertx,
                RpcService.ADDRESS,
                List.of(authenticationInterceptor, interceptorHolder, accessLogger))
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
