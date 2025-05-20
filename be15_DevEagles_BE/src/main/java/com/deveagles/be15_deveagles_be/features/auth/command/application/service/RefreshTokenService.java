package com.deveagles.be15_deveagles_be.features.auth.command.application.service;

public interface RefreshTokenService {
  void saveRefreshToken(String username, String refreshToken);

  String getRefreshToken(String username);

  void deleteRefreshToken(String username);
}
