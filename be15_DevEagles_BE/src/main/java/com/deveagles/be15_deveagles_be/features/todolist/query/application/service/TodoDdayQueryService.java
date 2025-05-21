package com.deveagles.be15_deveagles_be.features.todolist.query.application.service;

import com.deveagles.be15_deveagles_be.common.dto.PagedResponse;
import com.deveagles.be15_deveagles_be.common.dto.Pagination;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyDdayTodoResponse;
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

  public PagedResponse<MyDdayTodoResponse> getMyIncompleteTodosWithDday(
      Long userId, int page, int size) {
    int offset = (page - 1) * size;
    List<MyDdayTodoResponse> content =
        todoDdayQueryMapper.selectMyIncompleteTodosWithDday(userId, offset, size);
    int totalItems = todoDdayQueryMapper.countMyIncompleteTodos(userId);
    int totalPages = (int) Math.ceil((double) totalItems / size);

    Pagination pagination =
        Pagination.builder()
            .currentPage(page)
            .totalPages(totalPages)
            .totalItems(totalItems)
            .build();

    return new PagedResponse<>(content, pagination);
  }

  public PagedResponse<MyTeamDdayTodoResponse> getMyTeamIncompleteTodosWithDday(
      Long userId, Long teamId, int page, int size) {
    int offset = (page - 1) * size;
    List<MyTeamDdayTodoResponse> content =
        todoDdayQueryMapper.selectMyTeamIncompleteTodosWithDday(userId, teamId, offset, size);
    int totalItems = todoDdayQueryMapper.countMyTeamIncompleteTodos(userId, teamId);
    int totalPages = (int) Math.ceil((double) totalItems / size);

    Pagination pagination =
        Pagination.builder()
            .currentPage(page)
            .totalPages(totalPages)
            .totalItems(totalItems)
            .build();

    return new PagedResponse<>(content, pagination);
  }
}
