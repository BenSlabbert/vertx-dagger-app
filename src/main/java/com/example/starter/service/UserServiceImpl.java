package com.example.starter.service;

import com.example.starter.repository.UserRepository;
import com.example.starter.route.handler.dto.LoginRequestDto;
import com.example.starter.route.handler.dto.LoginResponseDto;
import com.example.starter.route.handler.dto.RefreshRequestDto;
import com.example.starter.route.handler.dto.RefreshResponseDto;
import com.example.starter.route.handler.dto.RegisterRequestDto;
import com.example.starter.route.handler.dto.RegisterResponseDto;
import io.vertx.core.Future;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Inject
  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Future<LoginResponseDto> login(LoginRequestDto user) {
    return userRepository.login(user);
  }

  @Override
  public Future<RefreshResponseDto> refresh(RefreshRequestDto user) {
    return userRepository.refresh(user);
  }

  @Override
  public Future<RegisterResponseDto> register(RegisterRequestDto user) {
    return userRepository.register(user);
  }
}
