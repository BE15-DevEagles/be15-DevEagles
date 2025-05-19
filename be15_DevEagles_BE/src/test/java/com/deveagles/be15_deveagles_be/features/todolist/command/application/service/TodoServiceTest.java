package com.deveagles.be15_deveagles_be.features.todolist.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.CreateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response.TodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.aggregate.Todo;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.repository.TodoRepository;
import com.deveagles.be15_deveagles_be.features.todolist.exception.InvalidTodoDateException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
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
  void createTodos_success() {
    when(todoRepository.saveAll(any()))
        .thenAnswer(
            invocation -> {
              List<Todo> inputTodos = invocation.getArgument(0);
              return inputTodos.stream()
                  .map(
                      todo ->
                          Todo.builder()
                              .todoId(123L)
                              .userId(todo.getUserId())
                              .teamId(todo.getTeamId())
                              .content(todo.getContent())
                              .startDate(todo.getStartDate())
                              .dueDate(todo.getDueDate())
                              .createdAt(LocalDateTime.now())
                              .modifiedAt(LocalDateTime.now())
                              .build())
                  .toList();
            });

    List<TodoResponse> responses = todoService.createTodos(List.of(validRequest));

    assertThat(responses).isNotEmpty();
    assertThat(responses.get(0).getTodoId()).isEqualTo(123L);
    assertThat(responses.get(0).getMessage()).isEqualTo("할 일이 등록되었습니다.");
  }

  @Test
  @DisplayName("시작일이 마감일보다 늦으면 예외 발생")
  void createTodos_invalidDate_throwsException() {
    assertThatThrownBy(() -> todoService.createTodos(List.of(invalidDateRequest)))
        .isInstanceOf(InvalidTodoDateException.class)
        .hasMessageContaining("시작일은 마감일보다 빠를 수 없습니다.");
  }

  @Test
  @DisplayName("여러 개 할 일 생성 성공")
  void createTodos_multiple_success() {
    CreateTodoRequest request1 =
        CreateTodoRequest.builder()
            .teamId(1L)
            .content("할 일 1")
            .startDate(LocalDateTime.of(2025, 6, 1, 9, 0))
            .dueDate(LocalDateTime.of(2025, 6, 2, 18, 0))
            .build();

    CreateTodoRequest request2 =
        CreateTodoRequest.builder()
            .teamId(2L)
            .content("할 일 2")
            .startDate(LocalDateTime.of(2025, 6, 3, 9, 0))
            .dueDate(LocalDateTime.of(2025, 6, 4, 18, 0))
            .build();

    AtomicLong idGenerator = new AtomicLong(100L);
    when(todoRepository.saveAll(any()))
        .thenAnswer(
            invocation -> {
              List<Todo> inputTodos = invocation.getArgument(0);
              return inputTodos.stream()
                  .map(
                      todo ->
                          Todo.builder()
                              .todoId(idGenerator.getAndIncrement())
                              .userId(todo.getUserId())
                              .teamId(todo.getTeamId())
                              .content(todo.getContent())
                              .startDate(todo.getStartDate())
                              .dueDate(todo.getDueDate())
                              .createdAt(LocalDateTime.now())
                              .modifiedAt(LocalDateTime.now())
                              .build())
                  .toList();
            });

    List<TodoResponse> responses = todoService.createTodos(List.of(request1, request2));

    assertThat(responses).hasSize(2);
    assertThat(responses.get(0).getTodoId()).isNotNull();
    assertThat(responses.get(1).getTodoId()).isNotNull();
    assertThat(responses.get(0).getMessage()).isEqualTo("할 일이 등록되었습니다.");
    assertThat(responses.get(1).getMessage()).isEqualTo("할 일이 등록되었습니다.");
  }
}
