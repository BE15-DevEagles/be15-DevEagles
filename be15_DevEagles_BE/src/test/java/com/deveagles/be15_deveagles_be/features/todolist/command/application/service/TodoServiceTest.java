package com.deveagles.be15_deveagles_be.features.todolist.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.CreateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.UpdateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response.TodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.aggregate.Todo;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.repository.TodoRepository;
import com.deveagles.be15_deveagles_be.features.todolist.exception.InvalidTodoDateException;
import com.deveagles.be15_deveagles_be.features.todolist.exception.TodoAlreadyCompletedException;
import com.deveagles.be15_deveagles_be.features.todolist.exception.TodoNotFoundException;
import com.deveagles.be15_deveagles_be.features.todolist.exception.TodoUnauthorizedAccessException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

  @InjectMocks private TodoService todoService;
  @Mock private TodoRepository todoRepository;

  private final Long userId = 1L;

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
            .startDate(LocalDateTime.of(2025, 5, 22, 10, 0))
            .dueDate(LocalDateTime.of(2025, 5, 21, 10, 0))
            .build();
  }

  @Test
  @DisplayName("할 일 생성 성공")
  void createTodos_success() {
    when(todoRepository.saveAll(any()))
        .thenAnswer(
            invocation -> {
              List<Todo> todos = invocation.getArgument(0);
              return todos.stream().map(todo -> todo.toBuilder().todoId(123L).build()).toList();
            });

    List<TodoResponse> responses = todoService.createTodos(userId, List.of(validRequest));

    assertThat(responses).isNotEmpty();
    assertThat(responses.get(0).getTodoId()).isEqualTo(123L);
    assertThat(responses.get(0).getMessage()).isEqualTo("할 일이 등록되었습니다.");
  }

  @Test
  @DisplayName("시작일이 마감일보다 늦으면 예외 발생")
  void createTodos_invalidDate_throwsException() {
    assertThatThrownBy(() -> todoService.createTodos(userId, List.of(invalidDateRequest)))
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

    AtomicLong idGen = new AtomicLong(100);
    when(todoRepository.saveAll(any()))
        .thenAnswer(
            invocation -> {
              List<Todo> todos = invocation.getArgument(0);
              return todos.stream()
                  .map(todo -> todo.toBuilder().todoId(idGen.getAndIncrement()).build())
                  .toList();
            });

    List<TodoResponse> responses = todoService.createTodos(userId, List.of(request1, request2));

    assertThat(responses).hasSize(2);
    assertThat(responses.get(0).getTodoId()).isNotNull();
    assertThat(responses.get(1).getTodoId()).isNotNull();
  }

  @Test
  @DisplayName("할 일 완료 성공")
  void completeTodo_success() {
    Todo todo =
        Todo.builder()
            .todoId(1L)
            .userId(userId)
            .teamId(1L)
            .content("완료할 일")
            .startDate(LocalDateTime.now())
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

    when(todoRepository.findById(1L)).thenReturn(java.util.Optional.of(todo));

    TodoResponse response = todoService.completeTodo(userId, 1L);

    assertThat(response).isNotNull();
    assertThat(response.getMessage()).isEqualTo("할 일이 완료 처리되었습니다.");
    assertThat(todo.getCompletedAt()).isNotNull();
  }

  @Test
  @DisplayName("존재하지 않는 할 일 완료 시 예외 발생")
  void completeTodo_notFound_throwsException() {
    when(todoRepository.findById(99L)).thenReturn(java.util.Optional.empty());

    assertThatThrownBy(() -> todoService.completeTodo(userId, 99L))
        .isInstanceOf(TodoNotFoundException.class);
  }

  @Test
  @DisplayName("이미 완료된 할 일을 완료 처리 시 예외 발생")
  void completeTodo_alreadyCompleted_throwsException() {
    Todo todo = Todo.builder().todoId(1L).userId(userId).completedAt(LocalDateTime.now()).build();

    when(todoRepository.findById(1L)).thenReturn(java.util.Optional.of(todo));

    assertThatThrownBy(() -> todoService.completeTodo(userId, 1L))
        .isInstanceOf(TodoAlreadyCompletedException.class);
  }

  @Test
  @DisplayName("다른 유저의 할 일을 완료하려고 하면 예외 발생")
  void completeTodo_userMismatch_throwsException() {
    Todo todo = Todo.builder().todoId(1L).userId(999L).build();

    when(todoRepository.findById(1L)).thenReturn(java.util.Optional.of(todo));

    assertThatThrownBy(() -> todoService.completeTodo(userId, 1L))
        .isInstanceOf(TodoUnauthorizedAccessException.class);
  }

  @Test
  @DisplayName("할 일 수정 성공")
  void updateTodo_success() {
    Todo todo =
        Todo.builder()
            .todoId(1L)
            .userId(userId)
            .content("기존")
            .startDate(LocalDateTime.now())
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

    UpdateTodoRequest request =
        UpdateTodoRequest.builder()
            .content("수정됨")
            .startDate(LocalDateTime.now())
            .dueDate(LocalDateTime.now().plusDays(2))
            .build();

    when(todoRepository.findById(1L)).thenReturn(java.util.Optional.of(todo));

    TodoResponse response = todoService.updateTodo(userId, 1L, request);

    assertThat(response).isNotNull();
    assertThat(todo.getContent()).isEqualTo("수정됨");
  }

  @Test
  @DisplayName("다른 유저의 할 일 수정 시 예외 발생")
  void updateTodo_userMismatch_throwsException() {
    Todo todo = Todo.builder().todoId(1L).userId(999L).build();

    UpdateTodoRequest request =
        UpdateTodoRequest.builder()
            .content("수정")
            .startDate(LocalDateTime.now())
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();

    when(todoRepository.findById(1L)).thenReturn(java.util.Optional.of(todo));

    assertThatThrownBy(() -> todoService.updateTodo(userId, 1L, request))
        .isInstanceOf(TodoUnauthorizedAccessException.class);
  }

  @Test
  @DisplayName("할 일 삭제 성공")
  void deleteTodo_success() {
    Todo todo = Todo.builder().todoId(1L).userId(userId).build();

    when(todoRepository.findById(1L)).thenReturn(java.util.Optional.of(todo));
    doNothing().when(todoRepository).delete(todo);

    TodoResponse response = todoService.deleteTodo(userId, 1L);

    assertThat(response).isNotNull();
    verify(todoRepository).delete(todo);
  }

  @Test
  @DisplayName("다른 유저의 할 일 삭제 시 예외 발생")
  void deleteTodo_userMismatch_throwsException() {
    Todo todo = Todo.builder().todoId(1L).userId(999L).build();

    when(todoRepository.findById(1L)).thenReturn(java.util.Optional.of(todo));

    assertThatThrownBy(() -> todoService.deleteTodo(userId, 1L))
        .isInstanceOf(TodoUnauthorizedAccessException.class);
  }
}
