package com.deveagles.be15_deveagles_be.features.comment.command.domain.aggregate;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Entity
@Table(name = "comment")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long commentId;

  @Column(name = "worklog_id")
  private Long worklogId;

  @Column(name = "comment_content")
  private String commentContent;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "user_id")
  private Long userId;

  @Builder
  public Comment(
      Long worklogId,
      String commentContent,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      LocalDateTime deletedAt,
      Long userId) {
    this.worklogId = worklogId;
    this.commentContent = commentContent;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.deletedAt = deletedAt;
    this.userId = userId;
  }
}
