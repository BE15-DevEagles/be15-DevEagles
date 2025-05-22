package com.deveagles.be15_deveagles_be.features.auth.command.application.service;

import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.LoginRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.UserFindIdRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.UserFindPwdRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.TokenResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.UserFindIdResponse;

public interface AuthService {
  TokenResponse login(LoginRequest request);

  void logout(String refreshToken, String accessToken);

  UserFindIdResponse findId(UserFindIdRequest request);

  String sendAuthEmail(String email);

  void verifyAuthCode(String email, String authCode);

  String sendFindPwdEmail(UserFindPwdRequest request);
}
