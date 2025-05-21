package com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response;

import java.time.LocalDateTime;

import com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate.Worklog;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorklogResponse {
  private Long worklogId;
  private String userName;
  private String teamName;
  private String summary;
  private LocalDateTime writtenAt;



}
