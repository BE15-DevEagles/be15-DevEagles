package com.deveagles.be15_deveagles_be.features.comment.command.domain.aggregate;

import jakarta.persistence.*;
import java.sql.Timestamp;
import lombok.Getter;

@Getter
@Entity
@Table(name = "comment")
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long commentId;

  @Column(name = "worklog_id")
  private Long worklogId;

  @Column(name = "comment_content")
  private String commentContent;

  @Column(name = "created_at")
  private Timestamp createdAt;

  @Column(name = "updated_at")
  private Timestamp updatedAt;

  @Column(name = "deleted_at")
  private Timestamp deletedAt;

  @Column(name = "user_id")
  private Long userId;
}
