package com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TodoDetailResponse {
  private Long todoId;
  private Long userId;
  private Long teamId;
  private String teamName;
  private String content;
  private LocalDateTime startDate;
  private LocalDateTime dueDate;
  private Boolean isCompleted;
  private String userName;
}
