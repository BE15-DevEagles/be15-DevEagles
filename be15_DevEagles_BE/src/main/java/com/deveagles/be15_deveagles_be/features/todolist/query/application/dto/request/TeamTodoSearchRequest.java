package com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.request;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamTodoSearchRequest {
  private Long teamId;
  private List<Long> userIds;
  private String status;
  private int offset;
  private int size;
}
