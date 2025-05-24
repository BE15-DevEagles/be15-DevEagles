package com.deveagles.be15_deveagles_be.features.todolist.query.application.service;

import com.deveagles.be15_deveagles_be.common.dto.PagedResponse;
import com.deveagles.be15_deveagles_be.common.dto.Pagination;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.TeamFilteredTodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.mapper.TodoTeamQueryMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TodoTeamQueryService {

  private final TodoTeamQueryMapper todoTeamQueryMapper;

  public TodoTeamQueryService(TodoTeamQueryMapper todoTeamQueryMapper) {
    this.todoTeamQueryMapper = todoTeamQueryMapper;
  }

  public PagedResponse<TeamFilteredTodoResponse> getTeamTodosByCondition(
      Long teamId, Long userId, String status, int page, int size) {

    int offset = (page - 1) * size;

    List<TeamFilteredTodoResponse> content =
        todoTeamQueryMapper.selectTeamTodosByCondition(teamId, userId, status, offset, size);

    int totalItems = todoTeamQueryMapper.countTeamTodosByCondition(teamId, userId, status);

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
