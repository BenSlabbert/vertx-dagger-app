package com.example.starter.service;

import com.example.starter.route.handler.dto.LoginRequestDto;
import com.example.starter.route.handler.dto.LoginResponseDto;
import com.example.starter.route.handler.dto.RefreshRequestDto;
import com.example.starter.route.handler.dto.RefreshResponseDto;
import com.example.starter.route.handler.dto.RegisterRequestDto;
import com.example.starter.route.handler.dto.RegisterResponseDto;
import io.vertx.core.Future;

public interface UserService {

  Future<LoginResponseDto> login(LoginRequestDto user);

  Future<RefreshResponseDto> refresh(RefreshRequestDto user);

  Future<RegisterResponseDto> register(RegisterRequestDto user);
}
