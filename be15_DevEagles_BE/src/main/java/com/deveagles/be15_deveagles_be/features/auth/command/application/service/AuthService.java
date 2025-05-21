package com.deveagles.be15_deveagles_be.features.auth.command.application.service;

import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.LoginRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.UserFindIdRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.TokenResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.UserFindIdResponse;
import jakarta.validation.Valid;

public interface AuthService {
  TokenResponse login(LoginRequest request);

  void logout(String refreshToken, String accessToken);

  UserFindIdResponse findId(@Valid UserFindIdRequest request);
}
