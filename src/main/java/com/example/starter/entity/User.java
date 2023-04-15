package com.example.starter.entity;

import lombok.Builder;

@Builder
public record User(String username, String password, String refreshToken) {

  public static final String USERNAME_FIELD = "username";
  public static final String PASSWORD_FIELD = "password";
  public static final String REFRESH_TOKEN_FIELD = "refreshToken";
}
