package com.deveagles.be15_deveagles_be.features.auth.command.application.service.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.LoginRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.TokenResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class AuthServiceTest {

  @Autowired private AuthService authService;

  private LoginRequest validRequest;

  @BeforeEach
  void setUp() {
    validRequest =
        LoginRequest.builder().username("nare20027@gmail.com").password("eagles1234!").build();
  }

  @DisplayName("로그인 테스트")
  @Test
  void testLogin() {

    TokenResponse response = authService.login(validRequest);

    assertThat(response).isNotNull();
  }
}
