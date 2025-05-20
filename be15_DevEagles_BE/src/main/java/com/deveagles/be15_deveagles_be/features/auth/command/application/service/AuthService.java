package com.deveagles.be15_deveagles_be.features.auth.command.application.service;

import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.LoginRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.TokenResponse;

public interface AuthService {
  TokenResponse login(LoginRequest request);

  void logout(String refreshToken, String accessToken);
}
