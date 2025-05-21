package com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "team")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Team {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "team_id")
  private Long teamId;

  @Column(name = "user_id", nullable = false)
  private Long userId; // 필요 시 @ManyToOne 관계 설정 가능

  @Column(name = "team_name", nullable = false)
  private String teamName;

  @Column(name = "URL")
  private String url;

  @Column(name = "introduction", nullable = false)
  private String introduction;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "modified_at", nullable = false)
  private LocalDateTime modifiedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.modifiedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void onUpdate() {
    this.modifiedAt = LocalDateTime.now();
  }

  public void updateLeader(Long userId) {
    this.userId = userId;
  }

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void setUrl(String fileUrl) {
    this.url = fileUrl;
  }
}
