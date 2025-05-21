package com.deveagles.be15_deveagles_be.features.auth.command.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.common.jwt.JwtTokenProvider;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.LoginRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.UserFindIdRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.TokenResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.UserFindIdResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.service.AuthServiceImpl;
import com.deveagles.be15_deveagles_be.features.auth.command.application.service.RefreshTokenService;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceImplTest {

  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private JwtTokenProvider jwtTokenProvider;
  private RefreshTokenService refreshTokenService;
  private RedisTemplate<String, String> redisTemplate;
  private AuthServiceImpl authService;

  private String email;
  private String password;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    jwtTokenProvider = mock(JwtTokenProvider.class);
    refreshTokenService = mock(RefreshTokenService.class);
    redisTemplate = mock(RedisTemplate.class);

    authService =
        new AuthServiceImpl(
            userRepository, passwordEncoder, jwtTokenProvider, refreshTokenService, redisTemplate);

    email = "test@email.com";
    password = "password123!";
  }

  @Test
  @DisplayName("로그인 성공")
  void testLoginSuccess() {
    // given
    LoginRequest request = new LoginRequest(email, password);
    User mockUser = mock(User.class);

    when(userRepository.findValidUserForLogin(eq(email), any(LocalDateTime.class)))
        .thenReturn(Optional.of(mockUser));
    when(mockUser.getPassword()).thenReturn("encodedPassword");
    when(mockUser.getEmail()).thenReturn(email);
    when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);
    when(jwtTokenProvider.createToken(email)).thenReturn("access-token");
    when(jwtTokenProvider.createRefreshToken(email)).thenReturn("refresh-token");

    // when
    TokenResponse response = authService.login(request);

    // then
    verify(refreshTokenService).saveRefreshToken(email, "refresh-token");
    assertThat(response.getAccessToken()).isEqualTo("access-token");
    assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
  }

  @Test
  @DisplayName("아이디(이메일) 찾기 성공")
  void testFindIdSuccess() {
    // given
    String userName = "홍길동";
    String phoneNumber = "01012345678";
    UserFindIdRequest request = new UserFindIdRequest(userName, phoneNumber);
    User mockUser = mock(User.class);

    when(userRepository.findValidUserForGetEmail(
            eq(userName), eq(phoneNumber), any(LocalDateTime.class)))
        .thenReturn(Optional.of(mockUser));
    when(mockUser.getEmail()).thenReturn(email);

    // when
    UserFindIdResponse response = authService.findId(request);

    // then
    assertThat(response.getEmail()).isEqualTo(email);
  }

  @Test
  @DisplayName("로그아웃 성공")
  void testLogoutSuccess() {
    // given
    String accessToken = "access-token";
    String refreshToken = "refresh-token";
    long remainingMillis = 3600000L; // 1시간

    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

    when(jwtTokenProvider.getUsernameFromJWT(refreshToken)).thenReturn(email);
    when(jwtTokenProvider.getRemainingExpiration(accessToken)).thenReturn(remainingMillis);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    // when
    authService.logout(refreshToken, accessToken);

    // then
    verify(jwtTokenProvider).validateToken(refreshToken);
    verify(refreshTokenService).deleteRefreshToken(email);
    verify(valueOperations)
        .set(eq("BL:" + accessToken), eq("logout"), eq(Duration.ofMillis(remainingMillis)));
  }
}
