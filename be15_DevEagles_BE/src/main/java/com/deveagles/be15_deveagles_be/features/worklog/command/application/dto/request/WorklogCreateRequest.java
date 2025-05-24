package com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorklogCreateRequest {

  private String summary;
  private String workContent;
  private String note;
  private String planContent;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime writtenAt;
}
