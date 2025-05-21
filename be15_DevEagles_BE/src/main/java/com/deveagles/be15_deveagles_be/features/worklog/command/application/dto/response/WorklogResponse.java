package com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorklogResponse {
  private String userName;
  private String summary;
  private LocalDateTime writtenAt;
}
