package com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeminiApiResponseDto {
  private List<Candidate> candidates;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Candidate {
    private Content content;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Content {
    private java.util.List<Part> parts;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Part {
    private String text;
  }
}
