package com.deveagles.be15_deveagles_be.features.team.command.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FireTeamMemberRequest {

  @Email(message = "이메일 형식이 올바르지 않습니다.")
  @NotNull(message = "이메일은 필수입니다.") private String email;
}
