package com.deveagles.be15_deveagles_be.features.timecapsule.query.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimecapsuleResponse {
  private Long timecapsuleId;
  private String timecapsuleContent;
  private LocalDate openDate;
  private String timecapsuleStatus;
  private LocalDateTime createdAt;
  private Long userId;
  private Long teamId;
  private LocalDateTime openedAt;
}
