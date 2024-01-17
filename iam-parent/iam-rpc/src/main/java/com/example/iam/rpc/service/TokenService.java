/* Licensed under Apache-2.0 2023. */
package com.example.iam.rpc.service;

import io.vertx.core.Future;
import io.vertx.ext.auth.User;

sealed interface TokenService permits TokenServiceImpl {

  Future<User> isValidToken(String token);
}
