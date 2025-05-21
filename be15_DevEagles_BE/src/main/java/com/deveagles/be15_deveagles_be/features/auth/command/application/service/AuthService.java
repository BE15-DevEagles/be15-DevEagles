package com.deveagles.be15_deveagles_be.features.auth.command.application.service;

import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.LoginRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.UserFindIdRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.TokenResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.UserFindIdResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface AuthService {
  TokenResponse login(LoginRequest request);

  void logout(String refreshToken, String accessToken);

  UserFindIdResponse findId(@Valid UserFindIdRequest request);

  String sendAuthEmail(@NotBlank(message = "사용자의 이메일 값은 필수입니다.") String email);

  void verifyAuthCode(
      @Email @NotBlank(message = "이메일이 전달되지 않았습니다.") String email,
      @NotBlank(message = "인증코드가 전달되지 않았습니다.") String s);
}
