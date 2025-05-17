package com.deveagles.be15_deveagles_be.features.todolist.command.domain.aggregate;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "todo")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Todo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "todo_id")
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "team_id", nullable = false)
  private Long teamId;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "due_date", nullable = false)
  private LocalDateTime dueDate;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "modified_at", nullable = false)
  private LocalDateTime modifiedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.modifiedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.modifiedAt = LocalDateTime.now();
  }

  public void complete() {
    this.completedAt = LocalDateTime.now();
  }

  public void updateContent(
      String newContent, LocalDateTime newStartDate, LocalDateTime newDueDate) {
    this.content = newContent;
    this.startDate = newStartDate;
    this.dueDate = newDueDate;
  }
}
