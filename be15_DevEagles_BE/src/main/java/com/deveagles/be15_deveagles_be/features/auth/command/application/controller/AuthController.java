package com.deveagles.be15_deveagles_be.features.auth.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request.LoginRequest;
import com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response.TokenResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.service.AuthService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody LoginRequest request) {

    TokenResponse response = authService.login(request);

    return buildTokenResponse(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @CookieValue(name = "refreshToken", required = false) String refreshToken) {

    if (refreshToken != null) {
      authService.logout(refreshToken);
    }
    ResponseCookie deleteCookie = createDeleteRefreshTokenCookie();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
        .body(ApiResponse.success(null));
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
