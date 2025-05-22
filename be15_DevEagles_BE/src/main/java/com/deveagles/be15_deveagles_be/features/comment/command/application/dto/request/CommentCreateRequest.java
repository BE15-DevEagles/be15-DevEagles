package com.deveagles.be15_deveagles_be.features.comment.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentCreateRequest {
  private Long worklogId;

  @NotBlank(message = "댓글 내용을 입력하세요")
  private String commentContent;
}
