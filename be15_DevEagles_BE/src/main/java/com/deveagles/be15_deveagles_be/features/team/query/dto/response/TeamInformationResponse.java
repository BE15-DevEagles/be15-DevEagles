package com.deveagles.be15_deveagles_be.features.team.query.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamInformationResponse {

  private Long teamId;
  private String teamName;
  private String introduction;
  private String url;
}
