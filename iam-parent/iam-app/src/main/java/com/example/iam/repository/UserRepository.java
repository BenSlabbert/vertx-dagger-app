/* Licensed under Apache-2.0 2023. */
package com.example.iam.repository;

import com.example.iam.entity.User;
import io.vertx.core.Future;
import java.util.Set;

public interface UserRepository {

  Future<User> findByUsername(String username);

  Future<Void> login(String username, String password, String token, String refreshToken);

  Future<Void> refresh(
      String username, String oldRefreshToken, String newToken, String newRefreshToken);

  Future<Void> register(
      String username,
      String password,
      String token,
      String refreshToken,
      String group,
      String role,
      Set<String> permissions);
}
