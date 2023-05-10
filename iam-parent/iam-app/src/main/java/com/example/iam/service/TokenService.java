package com.example.iam.service;

import io.vertx.core.Future;
import io.vertx.ext.auth.User;

public interface TokenService {

  Future<User> isValidToken(String token);

  Future<User> isValidRefresh(String token);

  String authToken(String username);

  String refreshToken(String username);
}
