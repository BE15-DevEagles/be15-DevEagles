package com.deveagles.be15_deveagles_be.features.todolist.query.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyCalendarTodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.TeamCalendarTodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.mapper.TodoCalendarQueryMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodoCalendarQueryServiceTest {

  @Mock private TodoCalendarQueryMapper todoCalendarQueryMapper;

  @InjectMocks private TodoCalendarQueryService todoCalendarQueryService;

  @Test
  @DisplayName("내 캘린더 todo 목록 조회 성공")
  void getMyCalendarTodos_success() {
    // given
    Long userId = 1L;
    List<MyCalendarTodoResponse> mockResponse =
        List.of(
            new MyCalendarTodoResponse(
                1L, "할 일 1", LocalDate.of(2025, 5, 21), LocalDate.of(2025, 5, 22), 10L),
            new MyCalendarTodoResponse(
                2L, "할 일 2", LocalDate.of(2025, 5, 23), LocalDate.of(2025, 5, 24), 11L));
    when(todoCalendarQueryMapper.selectMyCalendarTodos(userId)).thenReturn(mockResponse);

    // when
    List<MyCalendarTodoResponse> result = todoCalendarQueryService.getMyCalendarTodos(userId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getTodoId()).isEqualTo(1L);
    assertThat(result.get(1).getTeamId()).isEqualTo(11L);
  }

  @Test
  @DisplayName("특정 팀의 전체 캘린더 일정 조회 성공")
  void getTeamCalendarTodos_success() {
    Long teamId = 5L;

    List<TeamCalendarTodoResponse> mockList =
        List.of(
            TeamCalendarTodoResponse.builder()
                .todoId(201L)
                .content("팀 일정 1")
                .startDate(LocalDate.of(2025, 5, 21))
                .dueDate(LocalDate.of(2025, 5, 23))
                .build(),
            TeamCalendarTodoResponse.builder()
                .todoId(202L)
                .content("팀 일정 2")
                .startDate(LocalDate.of(2025, 5, 24))
                .dueDate(LocalDate.of(2025, 5, 25))
                .build());

    when(todoCalendarQueryMapper.selectTeamCalendarTodos(teamId)).thenReturn(mockList);

    List<TeamCalendarTodoResponse> result = todoCalendarQueryService.getTeamCalendarTodos(teamId);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getTodoId()).isEqualTo(201L);
    assertThat(result.get(1).getContent()).isEqualTo("팀 일정 2");
  }
}
