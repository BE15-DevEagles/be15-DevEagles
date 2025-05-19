package com.deveagles.be15_deveagles_be.features.auth.command.application.service;

import com.deveagles.be15_deveagles_be.common.jwt.JwtTokenProvider;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.LoginRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.TokenResponse;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;

  @Override
  public TokenResponse login(LoginRequest request) {

    User user =
        userRepository
            .findValidUserForLogin(request.username(), LocalDateTime.now().minusMonths(1))
            .orElseThrow(() -> new BadCredentialsException("로그인할 수 없는 계정입니다."));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new BadCredentialsException("올바르지 않은 비밀번호입니다.");
    }

    String accessToken = jwtTokenProvider.createToken(user.getEmail());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

    refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken);

    return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }
}
