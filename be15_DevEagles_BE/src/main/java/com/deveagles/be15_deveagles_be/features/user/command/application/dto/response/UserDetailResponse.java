package com.deveagles.be15_deveagles_be.features.user.command.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDetailResponse {

  private final Long userId;
  private final String email;
  private final String phoneNumber;
  private final String userName;
  private final String thumbnailUrl;
}
