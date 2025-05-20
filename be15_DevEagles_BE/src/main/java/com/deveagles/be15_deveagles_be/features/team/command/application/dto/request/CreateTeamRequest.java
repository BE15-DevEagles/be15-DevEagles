package com.deveagles.be15_deveagles_be.features.team.command.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateTeamRequest {

  @NotBlank(message = "팀 이름은 필수입니다.")
  private String teamName;

  @NotBlank(message = "팀 소개는 필수입니다.")
  private String introduction;
}
