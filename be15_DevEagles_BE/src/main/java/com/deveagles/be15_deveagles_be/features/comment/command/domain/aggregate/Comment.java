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

  private String commentContent;

  private Timestamp createdAt;

  private Timestamp updatedAt;

  private Timestamp deletedAt;

  @Column(name = "user_id")
  private Long userId;
}
