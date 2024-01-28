/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api;

import com.example.iam.auth.api.dto.LoginRequestDto;
import com.example.iam.auth.api.dto.LoginResponseDto;
import com.example.iam.auth.api.dto.RefreshRequestDto;
import com.example.iam.auth.api.dto.RefreshResponseDto;
import com.example.iam.auth.api.dto.RegisterRequestDto;
import com.example.iam.auth.api.dto.RegisterResponseDto;
import io.vertx.core.Future;

public interface IamAuthApi {

  Future<LoginResponseDto> login(LoginRequestDto user);

  Future<RefreshResponseDto> refresh(RefreshRequestDto user);

  Future<RegisterResponseDto> register(RegisterRequestDto user);
}
