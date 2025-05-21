package com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response;

import com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate.Worklog;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WorklogDetailResponse {
  private Long worklogId;
  private String summary;
  private String workContent;
  private String note;
  private String planContent;
  private LocalDateTime createdAt;
  private Long teamId;
  private Long userId;
  private String userName;
  private String teamName;
  private LocalDateTime writtenAt;

  public static WorklogDetailResponse of(Worklog worklog) {
    return WorklogDetailResponse.builder()
        .worklogId(worklog.getWorklogId())
        .summary(worklog.getSummary())
        .workContent(worklog.getWorkContent())
        .note(worklog.getNote())
        .planContent(worklog.getWorkContent())
        .createdAt(worklog.getCreatedAt())
        .teamId(worklog.getTeamId())
        .userId(worklog.getUserId())
        .build();
  }
}
