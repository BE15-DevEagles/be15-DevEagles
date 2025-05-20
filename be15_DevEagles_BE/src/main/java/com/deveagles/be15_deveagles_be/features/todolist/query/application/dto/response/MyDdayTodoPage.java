package com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyDdayTodoPage {
  private List<MyDdayTodoResponse> todos;
  private Pagination pagination;

  @Getter
  @Builder
  public static class Pagination {
    private int totalItems;
    private int totalPages;
    private int currentPage;
  }
}
