/* Licensed under Apache-2.0 2023. */
package com.example.iam.service;

import com.example.iam.web.route.dto.LoginRequestDto;
import com.example.iam.web.route.dto.LoginResponseDto;
import com.example.iam.web.route.dto.RefreshRequestDto;
import com.example.iam.web.route.dto.RefreshResponseDto;
import com.example.iam.web.route.dto.RegisterRequestDto;
import com.example.iam.web.route.dto.RegisterResponseDto;
import io.vertx.core.Future;

public interface UserService {

  Future<LoginResponseDto> login(LoginRequestDto user);

  Future<RefreshResponseDto> refresh(RefreshRequestDto user);

  Future<RegisterResponseDto> register(RegisterRequestDto user);
}
