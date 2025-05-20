package com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MyCalendarTodoResponse {
  private String content;
  private LocalDate startDate;
  private LocalDate dueDate;
  private Long teamId;
}
