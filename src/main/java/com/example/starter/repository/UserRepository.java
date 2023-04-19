package com.example.starter.repository;

import com.example.starter.web.route.dto.LoginRequestDto;
import com.example.starter.web.route.dto.LoginResponseDto;
import com.example.starter.web.route.dto.RefreshRequestDto;
import com.example.starter.web.route.dto.RefreshResponseDto;
import com.example.starter.web.route.dto.RegisterRequestDto;
import com.example.starter.web.route.dto.RegisterResponseDto;
import io.vertx.core.Future;

public interface UserRepository {

  Future<LoginResponseDto> login(LoginRequestDto user);

  Future<RefreshResponseDto> refresh(RefreshRequestDto user);

  Future<RegisterResponseDto> register(RegisterRequestDto user);

  Future<Boolean> isValidToken(String username, String token);
}
