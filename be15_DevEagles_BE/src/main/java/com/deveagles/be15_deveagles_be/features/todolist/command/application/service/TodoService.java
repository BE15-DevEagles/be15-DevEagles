package com.deveagles.be15_deveagles_be.features.todolist.command.application.service;

import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.CreateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.UpdateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response.TodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.aggregate.Todo;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.repository.TodoRepository;
import com.deveagles.be15_deveagles_be.features.todolist.exception.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoService {

  private final TodoRepository todoRepository;

  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof CustomUser customUser) {
      return customUser.getUserId();
    }
    throw new IllegalStateException("로그인된 사용자 정보를 찾을 수 없습니다.");
  }

  // 작성
  @Transactional
  public List<TodoResponse> createTodos(List<CreateTodoRequest> requests) {
    Long userId = getCurrentUserId();

    List<Todo> todos =
        requests.stream()
            .map(
                request -> {
                  if (request.getStartDate().isAfter(request.getDueDate())) {
                    throw new InvalidTodoDateException(TodoErrorCode.INVALID_TODO_DATE);
                  }

                  return Todo.builder()
                      .userId(userId)
                      .teamId(request.getTeamId())
                      .content(request.getContent())
                      .startDate(request.getStartDate())
                      .dueDate(request.getDueDate())
                      .build();
                })
            .toList();

    List<Todo> savedTodos = todoRepository.saveAll(todos);

    return savedTodos.stream()
        .map(
            saved ->
                TodoResponse.builder().todoId(saved.getTodoId()).message("할 일이 등록되었습니다.").build())
        .toList();
  }

  // 완료
  @Transactional
  public TodoResponse completeTodo(Long todoId) {
    Todo todo =
        todoRepository
            .findById(todoId)
            .orElseThrow(() -> new TodoNotFoundException(TodoErrorCode.TODO_NOT_FOUND));

    if (!todo.getUserId().equals(getCurrentUserId())) {
      throw new TodoUnauthorizedAccessException(TodoErrorCode.UNAUTHORIZED_TODO_ACCESS);
    }

    if (todo.getCompletedAt() != null) {
      throw new TodoAlreadyCompletedException(TodoErrorCode.TODO_ALREADY_COMPLETED);
    }

    todo.complete();

    return TodoResponse.builder().todoId(todo.getTodoId()).message("할 일이 완료 처리되었습니다.").build();
  }

  // 수정
  @Transactional
  public TodoResponse updateTodo(Long todoId, UpdateTodoRequest request) {
    Todo todo =
        todoRepository
            .findById(todoId)
            .orElseThrow(() -> new TodoNotFoundException(TodoErrorCode.TODO_NOT_FOUND));

    if (!todo.getUserId().equals(getCurrentUserId())) {
      throw new TodoUnauthorizedAccessException(TodoErrorCode.UNAUTHORIZED_TODO_ACCESS);
    }

    if (request.getStartDate().isAfter(request.getDueDate())) {
      throw new InvalidTodoDateException(TodoErrorCode.INVALID_TODO_DATE);
    }

    todo.updateContent(request.getContent(), request.getStartDate(), request.getDueDate());

    return TodoResponse.builder().todoId(todo.getTodoId()).message("할 일이 수정되었습니다.").build();
  }

  // 삭제
  @Transactional
  public TodoResponse deleteTodo(Long todoId) {
    Todo todo =
        todoRepository
            .findById(todoId)
            .orElseThrow(() -> new TodoNotFoundException(TodoErrorCode.TODO_NOT_FOUND));

    if (!todo.getUserId().equals(getCurrentUserId())) {
      throw new TodoUnauthorizedAccessException(TodoErrorCode.UNAUTHORIZED_TODO_ACCESS);
    }

    todoRepository.delete(todo);

    return TodoResponse.builder().todoId(todo.getTodoId()).message("할 일이 삭제되었습니다.").build();
  }
}
