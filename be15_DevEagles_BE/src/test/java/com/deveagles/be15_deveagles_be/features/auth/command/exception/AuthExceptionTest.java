package com.deveagles.be15_deveagles_be.features.auth.command.exception;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.LoginRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class AuthExceptionTest {

  @Autowired private AuthService authService;

  private LoginRequest invalidEmailRequest;
  private LoginRequest invalidPwdRequest;

  @BeforeEach
  void setUp() {
    invalidEmailRequest =
        LoginRequest.builder().username("eagles@email.com").password("eagles1234!").build();

    invalidPwdRequest =
        LoginRequest.builder().username("nare20027@gmail.com").password("eagles1234").build();
  }

  @DisplayName("존재하지 않는 아이디로 로그인 시 예외 발생 테스트")
  @Test
  void testLoginByInvalidEmail() {

    assertThatThrownBy(() -> authService.login(invalidEmailRequest))
        .isInstanceOf(BadCredentialsException.class);
  }

  @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생 테스트")
  @Test
  void testLoginByInvalidPwd() {

    assertThatThrownBy(() -> authService.login(invalidPwdRequest))
        .isInstanceOf(BadCredentialsException.class);
  }
}
