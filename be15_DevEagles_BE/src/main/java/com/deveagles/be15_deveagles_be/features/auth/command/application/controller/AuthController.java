package com.deveagles.be15_deveagles_be.features.auth.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.LoginRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.SendAuthEmailRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.UserFindIdRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.TokenResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.UserFindIdResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.auth.command.application.service.AuthCodeService;
import com.deveagles.be15_deveagles_be.features.auth.command.application.service.AuthService;
import com.deveagles.be15_deveagles_be.features.auth.command.application.service.MailService;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.UserStatus;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserErrorCode;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final AuthCodeService authCodeService;
  private final MailService mailService;
  private final UserRepository userRepository;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<TokenResponse>> login(
      @RequestBody @Valid LoginRequest request) {

    TokenResponse response = authService.login(request);

    return buildTokenResponse(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @CookieValue(name = "refreshToken", required = false) String refreshToken,
      @RequestHeader(name = "Authorization", required = false) String authHeader) {

    if (refreshToken != null && authHeader != null && authHeader.startsWith("Bearer ")) {
      String accessToken = authHeader.substring(7);
      authService.logout(refreshToken, accessToken);
    }

    ResponseCookie deleteCookie = createDeleteRefreshTokenCookie();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
        .body(ApiResponse.success(null));
  }

  @PostMapping("/findId")
  public ResponseEntity<ApiResponse<UserFindIdResponse>> findId(
      @RequestBody @Valid UserFindIdRequest request) {

    UserFindIdResponse response = authService.findId(request);

    return ResponseEntity.ok().body(ApiResponse.success(response));
  }

  @PostMapping("/valid")
  public ResponseEntity<ApiResponse<Boolean>> validUserStatus(
      @AuthenticationPrincipal CustomUser customUser) {

    Boolean is_valid;

    if (customUser.getUserStatus().equals(UserStatus.PENDING)) is_valid = Boolean.FALSE;
    else if (customUser.getUserStatus().equals(UserStatus.ENABLED)) is_valid = Boolean.TRUE;
    else return null;

    return ResponseEntity.ok().body(ApiResponse.success(is_valid));
  }

  @PostMapping("/sendAuth")
  public ResponseEntity<ApiResponse<String>> sendAuthEmail(
      @RequestBody SendAuthEmailRequest request) {

    String email = request.email();

    userRepository
        .findUserByEmail(email)
        .orElseThrow(() -> new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION));

    if (authCodeService.getAuthCode(email) != null) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.failure(
                  UserErrorCode.DUPLICATE_SEND_AUTH_EXCEPTION.getCode(),
                  UserErrorCode.DUPLICATE_USER_PHONE_EXCEPTION.getMessage()));
    }

    String authCode = UUID.randomUUID().toString().substring(0, 6);

    try {

      authCodeService.saveAuthCode(email, authCode);
      mailService.sendAuthMail(email, authCode);

      return ResponseEntity.ok().body(ApiResponse.success(authCode));

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.failure(
                  UserErrorCode.SEND_EMAIL_FAILURE_EXCEPTION.getCode(),
                  UserErrorCode.SEND_EMAIL_FAILURE_EXCEPTION.getMessage()));
    }
  }

  private ResponseEntity<ApiResponse<TokenResponse>> buildTokenResponse(TokenResponse response) {

    ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(ApiResponse.success(response));
  }

  private ResponseCookie createRefreshTokenCookie(String refreshToken) {

    return ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .path("/")
        .maxAge(Duration.ofDays(7))
        .sameSite("Strict")
        .build();
  }

  private ResponseCookie createDeleteRefreshTokenCookie() {

    return ResponseCookie.from("refreshToken", "")
        .httpOnly(true)
        .path("/")
        .maxAge(0)
        .sameSite("Strict")
        .build();
  }
}
