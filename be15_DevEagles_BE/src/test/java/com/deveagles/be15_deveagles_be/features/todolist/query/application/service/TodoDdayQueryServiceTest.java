package com.deveagles.be15_deveagles_be.features.todolist.query.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyDdayTodoPage;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyDdayTodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyTeamDdayTodoPage;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyTeamDdayTodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.mapper.TodoDdayQueryMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodoDdayQueryServiceTest {

  @Mock private TodoDdayQueryMapper todoDdayQueryMapper;

  @InjectMocks private TodoDdayQueryService todoDdayQueryService;

  @Test
  @DisplayName("내 미완료 todo 리스트 D-Day 포함 조회 성공")
  void getMyIncompleteTodosWithDday_success() {
    Long userId = 1L;
    int page = 1;
    int size = 5;
    int offset = 0;

    List<MyDdayTodoResponse> mockList =
        List.of(
            MyDdayTodoResponse.builder()
                .todoId(101L)
                .content("프론트 작업")
                .dueDate(LocalDate.of(2025, 5, 23))
                .teamId(5L)
                .dday(3)
                .build());

    when(todoDdayQueryMapper.selectMyIncompleteTodosWithDday(userId, offset, size))
        .thenReturn(mockList);
    when(todoDdayQueryMapper.countMyIncompleteTodos(userId)).thenReturn(1);

    MyDdayTodoPage result = todoDdayQueryService.getMyIncompleteTodosWithDday(userId, page, size);

    assertThat(result.getTodos()).hasSize(1);
    assertThat(result.getTodos().get(0).getTodoId()).isEqualTo(101L);
    assertThat(result.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(result.getPagination().getTotalPages()).isEqualTo(1);
    assertThat(result.getPagination().getCurrentPage()).isEqualTo(1);
  }

  @Test
  @DisplayName("특정 팀에서 내가 작성한 미완료 todo 리스트 D-Day 포함 조회 성공")
  void getMyTeamIncompleteTodosWithDday_success() {
    Long userId = 1L;
    Long teamId = 10L;
    int page = 1;
    int size = 5;
    int offset = 0;

    List<MyTeamDdayTodoResponse> mockList =
        List.of(
            MyTeamDdayTodoResponse.builder()
                .todoId(301L)
                .content("팀 작업 1")
                .dueDate(LocalDate.of(2025, 5, 25))
                .teamId(teamId)
                .dday(4)
                .build());

    when(todoDdayQueryMapper.selectMyTeamIncompleteTodosWithDday(userId, teamId, offset, size))
        .thenReturn(mockList);
    when(todoDdayQueryMapper.countMyTeamIncompleteTodos(userId, teamId)).thenReturn(1);

    MyTeamDdayTodoPage result =
        todoDdayQueryService.getMyTeamIncompleteTodosWithDday(userId, teamId, page, size);

    assertThat(result.getTodos()).hasSize(1);
    assertThat(result.getTodos().get(0).getTodoId()).isEqualTo(301L);
    assertThat(result.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(result.getPagination().getTotalPages()).isEqualTo(1);
    assertThat(result.getPagination().getCurrentPage()).isEqualTo(1);
  }
}
