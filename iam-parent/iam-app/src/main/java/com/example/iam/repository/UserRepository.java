/* Licensed under Apache-2.0 2023. */
package com.example.iam.repository;

import io.vertx.core.Future;

public interface UserRepository {

  Future<Void> login(String username, String password, String token, String refreshToken);

  Future<Void> refresh(
      String username, String oldRefreshToken, String newToken, String newRefreshToken);

  Future<Void> register(String username, String password, String token, String refreshToken);
}
