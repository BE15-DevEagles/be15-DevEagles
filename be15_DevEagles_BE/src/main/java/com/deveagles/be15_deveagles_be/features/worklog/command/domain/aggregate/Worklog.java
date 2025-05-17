package com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate;

import jakarta.persistence.*;
import java.sql.Timestamp;
import lombok.*;

@Getter
@Setter
@Table(name = "worklog")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Worklog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long worklogId;

  private String summary;

  @Column(name = "work_content", columnDefinition = "TEXT")
  private String workContent;

  @Column(name = "note", columnDefinition = "TEXT")
  private String note;

  @Column(name = "plan_content", columnDefinition = "TEXT")
  private String planContent;

  private Timestamp createdAt;

  @Column(name = "team_id")
  private Long teamId;

  @Column(name = "user_id")
  private Long userId;
}
