/* Licensed under Apache-2.0 2024. */
package com.example.commons.security;

import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import org.junit.jupiter.api.Test;

class SecurityHandlerTest {

  @Test
  void testSuccess() {
    JsonObject principal = new JsonObject().put("id", "test");
    JsonObject attributes = new JsonObject().put("attribute", "value");
    User user = User.create(principal, attributes);
    user.authorizations().add("role-provider-id", RoleBasedAuthorization.create("my-role"));

    Object id = user.get("id");
    assertThat(id).isNotNull();
    Object attribute = user.get("attribute");
    assertThat(attribute).isNotNull();

    RoutingContext ctx = mock(RoutingContext.class);
    when(ctx.user()).thenReturn(user);
    SecurityHandler.hasRole(ctx, RoleBasedAuthorization.create("my-role"));

    verify(ctx).next();
  }

  @Test
  void testFailure() {
    JsonObject principal = new JsonObject().put("id", "test");
    JsonObject attributes = new JsonObject().put("attribute", "value");
    User user = User.create(principal, attributes);
    user.authorizations().add("role-provider-id", RoleBasedAuthorization.create("my-role"));

    Object id = user.get("id");
    assertThat(id).isNotNull();
    Object attribute = user.get("attribute");
    assertThat(attribute).isNotNull();

    RoutingContext ctx = mock(RoutingContext.class);
    when(ctx.user()).thenReturn(user);
    SecurityHandler.hasRole(ctx, RoleBasedAuthorization.create("other-role"));

    verify(ctx)
        .fail(
            argThat(
                a -> {
                  assertThat(a).isInstanceOf(HttpException.class);
                  HttpException httpException = (HttpException) a;
                  assertThat(httpException.getStatusCode()).isEqualTo(UNAUTHORIZED.code());
                  return true;
                }));
  }
}
