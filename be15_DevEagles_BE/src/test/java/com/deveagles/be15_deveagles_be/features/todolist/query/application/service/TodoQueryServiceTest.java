package com.deveagles.be15_deveagles_be.features.todolist.query.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.todolist.exception.TodoErrorCode;
import com.deveagles.be15_deveagles_be.features.todolist.exception.TodoNotFoundException;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.TodoDetailResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.domain.repository.TodoQueryRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodoQueryServiceTest {

  @InjectMocks private TodoQueryService todoQueryService;

  @Mock private TodoQueryRepository todoQueryRepository;

  @Test
  @DisplayName("todo 상세 조회 성공")
  void getMyTodoDetail_success() {
    Long userId = 1L;
    Long todoId = 10L;
    TodoDetailResponse mockResponse =
        TodoDetailResponse.builder()
            .todoId(todoId)
            .userId(userId)
            .teamId(100L)
            .teamName("데브이글즈")
            .content("테스트 할 일")
            .startDate(LocalDateTime.of(2025, 5, 20, 10, 0))
            .dueDate(LocalDateTime.of(2025, 5, 21, 18, 0))
            .isCompleted(false)
            .build();

    when(todoQueryRepository.selectTodoDetail(todoId, userId)).thenReturn(mockResponse);

    TodoDetailResponse result = todoQueryService.getMyTodoDetail(userId, todoId);

    assertThat(result).isNotNull();
    assertThat(result.getTodoId()).isEqualTo(todoId);
    assertThat(result.getUserId()).isEqualTo(userId);
    assertThat(result.getTeamName()).isEqualTo("데브이글즈");
    assertThat(result.getContent()).isEqualTo("테스트 할 일");
  }

  @Test
  @DisplayName("존재하지 않는 todo 조회 시 예외 발생")
  void getMyTodoDetail_notFound() {
    Long userId = 1L;
    Long todoId = 99L;
    when(todoQueryRepository.selectTodoDetail(todoId, userId)).thenReturn(null);

    assertThatThrownBy(() -> todoQueryService.getMyTodoDetail(userId, todoId))
        .isInstanceOf(TodoNotFoundException.class)
        .hasMessageContaining(TodoErrorCode.TODO_NOT_FOUND.getMessage());
  }
}
