package com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyTeamDdayTodoResponse {
  private Long todoId;
  private String content;
  private LocalDate dueDate;
  private Long teamId;
  private long dday;
}
