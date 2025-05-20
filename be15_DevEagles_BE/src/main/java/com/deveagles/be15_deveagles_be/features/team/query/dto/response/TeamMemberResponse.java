package com.deveagles.be15_deveagles_be.features.team.query.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamMemberResponse {
  private Long userId;
  private String nickname;
  private String email;
  private String profileImageUrl;
}
