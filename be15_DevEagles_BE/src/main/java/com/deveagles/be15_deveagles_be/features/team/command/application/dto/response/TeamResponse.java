package com.deveagles.be15_deveagles_be.features.team.command.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamResponse {
  private Long teamId;
  private String teamName;
}
