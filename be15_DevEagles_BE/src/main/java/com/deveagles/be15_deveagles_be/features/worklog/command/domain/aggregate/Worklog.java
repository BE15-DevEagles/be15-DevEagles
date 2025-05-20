package com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Table(name = "worklog")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class Worklog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long worklogId;

  @Column(name = "summary")
  private String summary;

  @Column(name = "work_content", columnDefinition = "TEXT")
  private String workContent;

  @Column(name = "note", columnDefinition = "TEXT")
  private String note;

  @Column(name = "plan_content", columnDefinition = "TEXT")
  private String planContent;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "team_id")
  private Long teamId;

  @Column(name = "user_id")
  private Long userId;

  @Builder
  public Worklog(
      String summary,
      String workContent,
      String note,
      String planContent,
      LocalDateTime createdAt,
      Long teamId,
      Long userId) {
    this.summary = summary;
    this.workContent = workContent;
    this.note = note;
    this.planContent = planContent;
    this.createdAt = createdAt;
    this.teamId = teamId;
    this.userId = userId;
  }
}
