package com.example.iam.repository;

import com.example.iam.web.route.dto.LoginResponseDto;
import com.example.iam.web.route.dto.RefreshResponseDto;
import com.example.iam.web.route.dto.RegisterResponseDto;
import io.vertx.core.Future;

public interface UserRepository {

  Future<LoginResponseDto> login(
      String username, String password, String token, String refreshToken);

  Future<RefreshResponseDto> refresh(
      String username, String oldRefreshToken, String newToken, String newRefreshToken);

  Future<RegisterResponseDto> register(
      String username, String password, String token, String refreshToken);
}
