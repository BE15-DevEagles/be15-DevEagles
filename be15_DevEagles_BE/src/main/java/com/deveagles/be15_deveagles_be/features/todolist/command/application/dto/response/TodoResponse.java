package com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoResponse {
  private Long todoId;
  private String message;
}
