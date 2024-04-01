/* Licensed under Apache-2.0 2024. */
package com.example.commons.rpc;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.serviceproxy.ServiceInterceptor;
import java.util.Map;

public class UserAccessLoggerInterceptor implements ServiceInterceptor {

  private static final Logger log = LoggerFactory.getLogger(UserAccessLoggerInterceptor.class);

  private UserAccessLoggerInterceptor() {}

  public static ServiceInterceptor create() {
    return new UserAccessLoggerInterceptor();
  }

  @Override
  public Future<Message<JsonObject>> intercept(
      Vertx vertx, Map<String, Object> interceptorContext, Message<JsonObject> message) {
    final ContextInternal vertxContext = (ContextInternal) vertx.getOrCreateContext();

    User user = (User) interceptorContext.get("user");

    if (null == user) {
      return vertxContext.failedFuture(
          new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 403, "Forbidden"));
    }

    String action = message.headers().get("action");

    log.info(user.subject() + ": executing action: " + action);
    return vertxContext.succeededFuture(message);
  }
}
