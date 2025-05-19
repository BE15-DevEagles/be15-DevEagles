package com.deveagles.be15_deveagles_be.features.todolist.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.CreateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response.TodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.aggregate.Todo;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.repository.TodoRepository;
import com.deveagles.be15_deveagles_be.features.todolist.exception.InvalidTodoDateException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

  @InjectMocks private TodoService todoService;

  @Mock private TodoRepository todoRepository;

  private CreateTodoRequest validRequest;
  private CreateTodoRequest invalidDateRequest;

  @BeforeEach
  void setUp() {
    validRequest =
        CreateTodoRequest.builder()
            .teamId(10L)
            .content("테스트용 할 일")
            .startDate(LocalDateTime.of(2025, 5, 20, 10, 0))
            .dueDate(LocalDateTime.of(2025, 5, 21, 18, 0))
            .build();

    invalidDateRequest =
        CreateTodoRequest.builder()
            .teamId(1L)
            .content("잘못된 날짜")
            .startDate(LocalDateTime.of(2025, 5, 22, 10, 0)) // 시작일이 마감일보다 늦음
            .dueDate(LocalDateTime.of(2025, 5, 21, 10, 0))
            .build();
  }

  @Test
  @DisplayName("할 일 생성 성공")
  void createTodo_success() {
    when(todoRepository.save(any()))
        .thenAnswer(
            invocation -> {
              Todo saved = invocation.getArgument(0);
              Field idField = saved.getClass().getDeclaredField("todoId");
              idField.setAccessible(true);
              idField.set(saved, 123L);

              return saved;
            });

    // when
    TodoResponse response = todoService.createTodo(validRequest);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getTodoId()).isEqualTo(123L);
    assertThat(response.getMessage()).isEqualTo("할 일이 등록되었습니다.");
  }

  @Test
  @DisplayName("시작일이 마감일보다 늦으면 예외 발생")
  void createTodo_invalidDate_throwsException() {
    // when & then
    assertThatThrownBy(() -> todoService.createTodo(invalidDateRequest))
        .isInstanceOf(InvalidTodoDateException.class)
        .hasMessageContaining("시작일은 마감일보다 빠를 수 없습니다.");
  }
}
