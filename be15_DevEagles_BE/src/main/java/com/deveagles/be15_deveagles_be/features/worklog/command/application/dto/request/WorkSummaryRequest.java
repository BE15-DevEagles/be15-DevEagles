package com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WorkSummaryRequest {
  @NotBlank(message = "업무내용은 필수입니다.")
  private String workContent;

  @NotBlank(message = "특이사항은 필수입니다.")
  private String note;
}
