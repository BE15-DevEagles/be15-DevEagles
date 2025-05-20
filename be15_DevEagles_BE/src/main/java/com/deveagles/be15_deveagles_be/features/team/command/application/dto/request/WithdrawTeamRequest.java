package com.deveagles.be15_deveagles_be.features.team.command.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class WithdrawTeamRequest {

  @NotNull(message = "팀 ID는 필수입니다.") private Long teamId;
}
