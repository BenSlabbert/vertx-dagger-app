/* Licensed under Apache-2.0 2023. */
package com.example.iam.service;

import com.example.iam.entity.ACL;
import com.example.iam.entity.User;
import com.example.iam.repository.UserRepository;
import github.benslabbert.vertxdaggerapp.api.iam.auth.IamAuthApi;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.LoginRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.LoginResponseDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.RefreshRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.RefreshResponseDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.RegisterRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.RegisterResponseDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.UpdatePermissionsRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.UpdatePermissionsResponseDto;
import io.vertx.core.Future;
import java.util.Set;
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
    var group = access.group();
    var role = access.role();
    Set<String> permissions = access.permissions();

    String token =
        tokenService.authToken(
            dto.username(), ACL.builder().group(group).role(role).permissions(permissions).build());
    String refreshToken = tokenService.refreshToken(dto.username());

    return userRepository
        .register(dto.username(), dto.password(), token, refreshToken, group, role, permissions)
        .map(ignore -> RegisterResponseDto.builder().build());
  }

  @Override
  public Future<UpdatePermissionsResponseDto> updatePermissions(UpdatePermissionsRequestDto req) {
    return userRepository
        .findByUsername(req.username())
        .map(
            ignore ->
                ACL.builder()
                    .group(req.access().group())
                    .role(req.access().role())
                    .permissions(req.access().permissions())
                    .build())
        .compose(acl -> userRepository.updatePermissions(req.username(), acl))
        .map(ignore -> UpdatePermissionsResponseDto.builder().build());
  }
}
