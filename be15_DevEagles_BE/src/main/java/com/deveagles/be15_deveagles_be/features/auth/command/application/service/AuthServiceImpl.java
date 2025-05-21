package com.deveagles.be15_deveagles_be.features.auth.command.application.service;

import com.deveagles.be15_deveagles_be.common.jwt.JwtTokenProvider;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.LoginRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.UserFindIdRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.UserFindPwdRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.TokenResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.UserFindIdResponse;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.UserStatus;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserErrorCode;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import jakarta.mail.MessagingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
  private final RedisTemplate<String, String> redisTemplate;
  private final AuthCodeService authCodeService;
  private final MailService mailService;

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

  @Override
  public void logout(String refreshToken, String accessToken) {

    jwtTokenProvider.validateToken(refreshToken);
    String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);
    refreshTokenService.deleteRefreshToken(username);

    long remainTime = jwtTokenProvider.getRemainingExpiration(accessToken);
    redisTemplate.opsForValue().set("BL:" + accessToken, "logout", Duration.ofMillis(remainTime));
  }

  @Override
  public UserFindIdResponse findId(UserFindIdRequest request) {

    User validUser =
        userRepository
            .findValidUserForGetEmail(
                request.userName(), request.phoneNumber(), LocalDateTime.now().minusMonths(1))
            .orElseThrow(() -> new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION));

    return UserFindIdResponse.builder().email(validUser.getEmail()).build();
  }

  @Override
  public String findPwd(UserFindPwdRequest request) {

    userRepository
        .findValidUserForGetPwd(
            request.userName(), request.email(), LocalDateTime.now().minusMonths(1))
        .orElseThrow(() -> new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION));

    return sendAuthEmail(request.email());
  }

  @Override
  public String sendAuthEmail(String email) {
    userRepository
        .findUserByEmail(email)
        .orElseThrow(() -> new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION));

    if (authCodeService.getAuthCode(email) != null) {
      throw new UserBusinessException(UserErrorCode.DUPLICATE_SEND_AUTH_EXCEPTION);
    }

    String authCode = UUID.randomUUID().toString().substring(0, 6);

    authCodeService.saveAuthCode(email, authCode);
    try {
      mailService.sendAuthMail(email, authCode);
    } catch (MessagingException e) {
      throw new UserBusinessException(UserErrorCode.SEND_EMAIL_FAILURE_EXCEPTION);
    }

    return authCode;
  }

  @Override
  public void verifyAuthCode(String email, String authCode) {
    String savedCode = authCodeService.getAuthCode(email);

    if (savedCode == null || !savedCode.equals(authCode)) {
      throw new UserBusinessException(UserErrorCode.INVALID_AUTH_CODE);
    }

    User user =
        userRepository
            .findUserByEmail(email)
            .orElseThrow(() -> new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION));

    user.setEnabledUser(UserStatus.ENABLED);
    userRepository.save(user);

    authCodeService.deleteAuthCode(email);
  }
}
