package com.deveagles.be15_deveagles_be.features.comment.command.application.dto.response;

import com.deveagles.be15_deveagles_be.features.comment.command.domain.aggregate.Comment;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponse {
  private Long commentId;
  private String commentContent;
  private LocalDateTime time;
  private String username;
  private boolean isEdited;

  public static CommentResponse fromEntity(
      Comment comment, LocalDateTime time, String userName, boolean isEdited) {
    return CommentResponse.builder()
        .commentId(comment.getCommentId())
        .commentContent(comment.getCommentContent())
        .time(time)
        .username(userName)
        .isEdited(isEdited)
        .build();
  }
}
