/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api;

import com.example.iam.auth.api.dto.LoginRequestDto;
import com.example.iam.auth.api.dto.LoginResponseDto;
import com.example.iam.auth.api.dto.RefreshRequestDto;
import com.example.iam.auth.api.dto.RefreshResponseDto;
import com.example.iam.auth.api.dto.RegisterRequestDto;
import com.example.iam.auth.api.dto.RegisterResponseDto;
import com.example.iam.auth.api.dto.UpdatePermissionsRequestDto;
import com.example.iam.auth.api.dto.UpdatePermissionsResponseDto;
import io.vertx.core.Future;

public interface IamAuthApi {

  Future<LoginResponseDto> login(LoginRequestDto req);

  Future<RefreshResponseDto> refresh(RefreshRequestDto req);

  Future<RegisterResponseDto> register(RegisterRequestDto req);

  Future<UpdatePermissionsResponseDto> updatePermissions(UpdatePermissionsRequestDto req);
}
