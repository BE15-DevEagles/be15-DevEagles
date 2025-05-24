package com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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

  @JsonCreator
  public WorklogCreateRequest(
      @JsonProperty("summary") String summary,
      @JsonProperty("workContent") String workContent,
      @JsonProperty("note") String note,
      @JsonProperty("planContent") String planContent,
      @JsonProperty("writtenAt") LocalDateTime writtenAt) {
    this.summary = summary;
    this.workContent = workContent;
    this.note = note;
    this.planContent = planContent;
    this.writtenAt = writtenAt;
  }
}
