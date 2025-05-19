package com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTodoRequest {
  private String content;
  private LocalDateTime startDate;
  private LocalDateTime dueDate;
}
