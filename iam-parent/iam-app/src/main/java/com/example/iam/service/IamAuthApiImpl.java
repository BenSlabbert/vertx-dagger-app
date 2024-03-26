/* Licensed under Apache-2.0 2023. */
package com.example.iam.service;

import com.example.iam.auth.api.IamAuthApi;
import com.example.iam.auth.api.dto.LoginRequestDto;
import com.example.iam.auth.api.dto.LoginResponseDto;
import com.example.iam.auth.api.dto.RefreshRequestDto;
import com.example.iam.auth.api.dto.RefreshResponseDto;
import com.example.iam.auth.api.dto.RegisterRequestDto;
import com.example.iam.auth.api.dto.RegisterResponseDto;
import com.example.iam.auth.api.perms.Permission;
import com.example.iam.entity.ACL;
import com.example.iam.entity.User;
import com.example.iam.repository.UserRepository;
import io.vertx.core.Future;
import java.util.Set;
import java.util.stream.Collectors;
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
  public Future<LoginResponseDto> login(LoginRequestDto dto) {
    String refreshToken = tokenService.refreshToken(dto.username());

    return userRepository
        .findByUsername(dto.username())
        .map(user -> tokenService.authToken(user.username(), user.acl()))
        .compose(
            token ->
                userRepository
                    .login(dto.username(), dto.password(), token, refreshToken)
                    // i hate this
                    .map(ignore -> token))
        .map(token -> LoginResponseDto.builder().token(token).refreshToken(refreshToken).build());
  }

  @Override
  public Future<RefreshResponseDto> refresh(RefreshRequestDto dto) {
    return userRepository
        .findByUsername(dto.username())
        .compose(user -> refresh(user, dto.token()));
  }

  private Future<RefreshResponseDto> refresh(User user, String refreshToken) {
    return tokenService
        .isValidRefresh(refreshToken)
        .compose(
            u -> {
              String token = tokenService.authToken(user.username(), user.acl());
              String newRefreshToken = tokenService.refreshToken(user.username());
              return userRepository
                  .refresh(user.username(), refreshToken, token, refreshToken)
                  .map(
                      ignore ->
                          RefreshResponseDto.builder()
                              .token(token)
                              .refreshToken(newRefreshToken)
                              .build());
            });
  }

  @Override
  public Future<RegisterResponseDto> register(RegisterRequestDto dto) {
    var access = dto.access();
    var group = access.group().name();
    var role = access.role().name();
    Set<String> permissions =
        access.permissions().stream().map(Permission::name).collect(Collectors.toSet());

    String token =
        tokenService.authToken(
            dto.username(), ACL.builder().group(group).role(role).permissions(permissions).build());
    String refreshToken = tokenService.refreshToken(dto.username());

    return userRepository
        .register(dto.username(), dto.password(), token, refreshToken, group, role, permissions)
        .map(ignore -> RegisterResponseDto.builder().build());
  }
}
