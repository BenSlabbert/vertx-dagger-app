/* Licensed under Apache-2.0 2023. */
package com.example.iam.service;

import com.example.iam.repository.UserRepository;
import com.example.iam.web.route.dto.LoginRequestDto;
import com.example.iam.web.route.dto.LoginResponseDto;
import com.example.iam.web.route.dto.RefreshRequestDto;
import com.example.iam.web.route.dto.RefreshResponseDto;
import com.example.iam.web.route.dto.RegisterRequestDto;
import com.example.iam.web.route.dto.RegisterResponseDto;
import io.vertx.core.Future;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final TokenService tokenService;

  @Inject
  public UserServiceImpl(UserRepository userRepository, TokenService tokenService) {
    this.userRepository = userRepository;
    this.tokenService = tokenService;
  }

  @Override
  public Future<LoginResponseDto> login(LoginRequestDto user) {
    String token = tokenService.authToken(user.username());
    String refreshToken = tokenService.refreshToken(user.username());
    return userRepository.login(user.username(), user.password(), token, refreshToken);
  }

  @Override
  public Future<RefreshResponseDto> refresh(RefreshRequestDto user) {
    return tokenService
        .isValidRefresh(user.token())
        .compose(
            u -> {
              String token = tokenService.authToken(user.username());
              String refreshToken = tokenService.refreshToken(user.username());
              return userRepository.refresh(user.username(), user.token(), token, refreshToken);
            });
  }

  @Override
  public Future<RegisterResponseDto> register(RegisterRequestDto user) {
    String token = tokenService.authToken(user.username());
    String refreshToken = tokenService.refreshToken(user.username());
    return userRepository.register(user.username(), user.password(), token, refreshToken);
  }
}
