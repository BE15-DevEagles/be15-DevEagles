package com.deveagles.be15_deveagles_be.features.auth.command.exception;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.common.jwt.JwtTokenProvider;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.LoginRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.UserFindIdRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.service.AuthServiceImpl;
import com.deveagles.be15_deveagles_be.features.auth.command.application.service.RefreshTokenService;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserErrorCode;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthExceptionTest {

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
  @DisplayName("로그인 실패 - 존재하지 않는 사용자")
  void testLoginUserNotFound() {
    LoginRequest request = new LoginRequest(email, password);

    when(userRepository.findValidUserForLogin(eq(email), any(LocalDateTime.class)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessageContaining("로그인할 수 없는 계정입니다.");
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void testLoginPasswordMismatch() {
    LoginRequest request = new LoginRequest(email, password);
    User mockUser = mock(User.class);

    when(userRepository.findValidUserForLogin(eq(email), any(LocalDateTime.class)))
        .thenReturn(Optional.of(mockUser));
    when(mockUser.getPassword()).thenReturn("encodedPassword");
    when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(false);

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessageContaining("올바르지 않은 비밀번호입니다.");
  }

  @Test
  @DisplayName("아이디(이메일) 찾기 실패 - 사용자 없음")
  void testFindIdUserNotFound() {
    UserFindIdRequest request = new UserFindIdRequest("홍길동", "01012345678");

    when(userRepository.findValidUserForGetEmail(
            eq("홍길동"), eq("01012345678"), any(LocalDateTime.class)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.findId(request))
        .isInstanceOf(UserBusinessException.class)
        .hasMessageContaining(UserErrorCode.NOT_FOUND_USER_EXCEPTION.getMessage());
  }
}
