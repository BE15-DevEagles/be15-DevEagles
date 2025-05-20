package com.deveagles.be15_deveagles_be.features.auth.command.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TokenResponse {
  private String accessToken;
  private String refreshToken;
}
