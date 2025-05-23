package com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamTodoSearchRequest {
  private Long teamId;
  private Long userId;
  private String status;
  private int offset;
  private int size;
}
