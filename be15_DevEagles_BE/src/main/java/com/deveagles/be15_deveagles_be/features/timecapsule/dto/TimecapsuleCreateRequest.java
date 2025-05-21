package com.deveagles.be15_deveagles_be.features.timecapsule.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimecapsuleCreateRequest {
  @NotBlank(message = "타임캡슐 내용은 필수입니다.")
  private String timecapsuleContent;

  @NotNull(message = "오픈 날짜는 필수입니다.") @Future(message = "오픈 날짜는 미래여야 합니다.")
  private LocalDate openDate;

  @NotNull(message = "사용자 ID는 필수입니다.") private Long userId;

  @NotNull(message = "팀 ID는 필수입니다.") private Long teamId;
}
