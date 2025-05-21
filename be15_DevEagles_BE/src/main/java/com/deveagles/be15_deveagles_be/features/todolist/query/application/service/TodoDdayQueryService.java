package com.deveagles.be15_deveagles_be.features.todolist.query.application.service;

import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyDdayTodoPage;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyDdayTodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyTeamDdayTodoPage;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyTeamDdayTodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.mapper.TodoDdayQueryMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TodoDdayQueryService {

  private final TodoDdayQueryMapper todoDdayQueryMapper;

  public TodoDdayQueryService(TodoDdayQueryMapper todoDdayQueryMapper) {
    this.todoDdayQueryMapper = todoDdayQueryMapper;
  }

  public MyDdayTodoPage getMyIncompleteTodosWithDday(Long userId, int page, int size) {
    int offset = (page - 1) * size;
    List<MyDdayTodoResponse> todos =
        todoDdayQueryMapper.selectMyIncompleteTodosWithDday(userId, offset, size);
    int totalItems = todoDdayQueryMapper.countMyIncompleteTodos(userId);
    int totalPages = (int) Math.ceil((double) totalItems / size);

    return MyDdayTodoPage.builder()
        .todos(todos)
        .pagination(
            MyDdayTodoPage.Pagination.builder()
                .totalItems(totalItems)
                .totalPages(totalPages)
                .currentPage(page)
                .build())
        .build();
  }

  public MyTeamDdayTodoPage getMyTeamIncompleteTodosWithDday(
      Long userId, Long teamId, int page, int size) {
    int offset = (page - 1) * size;
    List<MyTeamDdayTodoResponse> todos =
        todoDdayQueryMapper.selectMyTeamIncompleteTodosWithDday(userId, teamId, offset, size);
    int totalItems = todoDdayQueryMapper.countMyTeamIncompleteTodos(userId, teamId);
    int totalPages = (int) Math.ceil((double) totalItems / size);

    return MyTeamDdayTodoPage.builder()
        .todos(todos)
        .pagination(
            MyTeamDdayTodoPage.Pagination.builder()
                .totalItems(totalItems)
                .totalPages(totalPages)
                .currentPage(page)
                .build())
        .build();
  }
}
