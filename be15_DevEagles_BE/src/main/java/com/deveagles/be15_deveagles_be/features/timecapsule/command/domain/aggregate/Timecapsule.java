package com.deveagles.be15_deveagles_be.features.timecapsule.command.domain.aggregate;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "timecapsule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timecapsule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "timecapsule_id")
  private Long timecapsuleId;

  @Column(name = "timecapsule_content", nullable = false, length = 255)
  private String timecapsuleContent;

  @Column(name = "open_date", nullable = false)
  private LocalDate openDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "timecapsule_status", nullable = false)
  private TimecapsuleStatus timecapsuleStatus;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "team_id", nullable = false)
  private Long teamId;

  @Column(name = "opened_at")
  private LocalDateTime openedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  public enum TimecapsuleStatus {
    ACTIVE,
    INACTIVE
  }
}
