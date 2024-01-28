/* Licensed under Apache-2.0 2023. */
package com.example.iam.service;

import com.example.iam.auth.api.IamAuthApi;
import com.example.iam.auth.api.dto.LoginRequestDto;
import com.example.iam.auth.api.dto.LoginResponseDto;
import com.example.iam.auth.api.dto.RefreshRequestDto;
import com.example.iam.auth.api.dto.RefreshResponseDto;
import com.example.iam.auth.api.dto.RegisterRequestDto;
import com.example.iam.auth.api.dto.RegisterResponseDto;
import com.example.iam.repository.UserRepository;
import io.vertx.core.Future;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class IamAuthApiImpl implements IamAuthApi {

  private final UserRepository userRepository;
  private final TokenService tokenService;

  @Inject
  IamAuthApiImpl(UserRepository userRepository, TokenService tokenService) {
    this.userRepository = userRepository;
    this.tokenService = tokenService;
  }

  @Override
  public Future<LoginResponseDto> login(LoginRequestDto user) {
    String token = tokenService.authToken(user.username());
    String refreshToken = tokenService.refreshToken(user.username());
    return userRepository
        .login(user.username(), user.password(), token, refreshToken)
        .map(ignore -> LoginResponseDto.builder().token(token).refreshToken(refreshToken).build());
  }

  @Override
  public Future<RefreshResponseDto> refresh(RefreshRequestDto user) {
    return tokenService
        .isValidRefresh(user.token())
        .compose(
            u -> {
              String token = tokenService.authToken(user.username());
              String refreshToken = tokenService.refreshToken(user.username());
              return userRepository
                  .refresh(user.username(), user.token(), token, refreshToken)
                  .map(
                      ignore ->
                          RefreshResponseDto.builder()
                              .token(user.token())
                              .refreshToken(refreshToken)
                              .build());
            });
  }

  @Override
  public Future<RegisterResponseDto> register(RegisterRequestDto user) {
    String token = tokenService.authToken(user.username());
    String refreshToken = tokenService.refreshToken(user.username());
    return userRepository
        .register(user.username(), user.password(), token, refreshToken)
        .map(ignore -> RegisterResponseDto.builder().build());
  }
}
