/* Licensed under Apache-2.0 2024. */
package com.example.commons.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;

/**
 * this AuthenticationProvider does no credential checks and always returns an authenticated user
 * with no permissions
 */
public final class NoAuthRequiredAuthenticationProvider implements AuthenticationProvider {

  private NoAuthRequiredAuthenticationProvider() {}

  public static NoAuthRequiredAuthenticationProvider create() {
    return new NoAuthRequiredAuthenticationProvider();
  }

  @Override
  public void authenticate(JsonObject jsonObject, Handler<AsyncResult<User>> handler) {
    handler.handle(
        new AsyncResult<>() {
          @Override
          public User result() {
            return User.fromName("no-auth-user");
          }

          @Override
          public Throwable cause() {
            return null;
          }

          @Override
          public boolean succeeded() {
            return true;
          }

          @Override
          public boolean failed() {
            return false;
          }
        });
  }

  @Override
  public Future<User> authenticate(JsonObject credentials) {
    Promise<User> promise = Promise.promise();
    this.authenticate(credentials, promise);
    return promise.future();
  }

  @Override
  public void authenticate(Credentials credentials, Handler<AsyncResult<User>> resultHandler) {
    this.authenticate(credentials).onComplete(resultHandler);
  }

  @Override
  public Future<User> authenticate(Credentials credentials) {
    return this.authenticate(credentials.toJson());
  }
}
