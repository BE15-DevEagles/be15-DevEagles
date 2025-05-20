package com.deveagles.be15_deveagles_be.features.todolist.query.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyCalendarTodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.mapper.TodoCalendarQueryMapper;
import java.time.LocalDate;
import java.util.Arrays;
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
        Arrays.asList(
            new MyCalendarTodoResponse(
                "할 일 1", LocalDate.of(2025, 5, 21), LocalDate.of(2025, 5, 22), 10L),
            new MyCalendarTodoResponse(
                "할 일 2", LocalDate.of(2025, 5, 23), LocalDate.of(2025, 5, 24), 11L));

    when(todoCalendarQueryMapper.selectMyCalendarTodos(userId)).thenReturn(mockResponse);

    List<MyCalendarTodoResponse> result = todoCalendarQueryService.getMyCalendarTodos(userId);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getContent()).isEqualTo("할 일 1");
    assertThat(result.get(1).getTeamId()).isEqualTo(11L);
  }
}
