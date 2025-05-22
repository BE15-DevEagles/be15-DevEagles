package com.deveagles.be15_deveagles_be.features.timecapsule.query.domain.aggregate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimecapsuleReadModel {
  private Long timecapsuleId;
  private String timecapsuleContent;
  private LocalDate openDate;
  private String timecapsuleStatus;
  private LocalDateTime createdAt;
  private Long userId;
  private Long teamId;
  private LocalDateTime openedAt;
}
