package com.deveagles.be15_deveagles_be.features.todolist.command.application.service;

import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.CreateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.UpdateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response.TodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.aggregate.Todo;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.repository.TodoRepository;
import com.deveagles.be15_deveagles_be.features.todolist.exception.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoService {

  private final TodoRepository todoRepository;

  @Transactional
  public List<TodoResponse> createTodos(Long userId, List<CreateTodoRequest> requests) {
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

    return todoRepository.saveAll(todos).stream()
        .map(
            todo ->
                TodoResponse.builder().todoId(todo.getTodoId()).message("할 일이 등록되었습니다.").build())
        .toList();
  }

  @Transactional
  public TodoResponse completeTodo(Long userId, Long todoId) {
    Todo todo =
        todoRepository
            .findById(todoId)
            .orElseThrow(() -> new TodoNotFoundException(TodoErrorCode.TODO_NOT_FOUND));

    if (!todo.getUserId().equals(userId)) {
      throw new TodoUnauthorizedAccessException(TodoErrorCode.UNAUTHORIZED_TODO_ACCESS);
    }

    if (todo.getCompletedAt() != null) {
      throw new TodoAlreadyCompletedException(TodoErrorCode.TODO_ALREADY_COMPLETED);
    }

    todo.complete();
    return TodoResponse.builder().todoId(todo.getTodoId()).message("할 일이 완료 처리되었습니다.").build();
  }

  @Transactional
  public TodoResponse updateTodo(Long userId, Long todoId, UpdateTodoRequest request) {
    Todo todo =
        todoRepository
            .findById(todoId)
            .orElseThrow(() -> new TodoNotFoundException(TodoErrorCode.TODO_NOT_FOUND));

    if (!todo.getUserId().equals(userId)) {
      throw new TodoUnauthorizedAccessException(TodoErrorCode.UNAUTHORIZED_TODO_ACCESS);
    }

    if (request.getStartDate().isAfter(request.getDueDate())) {
      throw new InvalidTodoDateException(TodoErrorCode.INVALID_TODO_DATE);
    }

    todo.updateContent(request.getContent(), request.getStartDate(), request.getDueDate());
    return TodoResponse.builder().todoId(todo.getTodoId()).message("할 일이 수정되었습니다.").build();
  }

  @Transactional
  public TodoResponse deleteTodo(Long userId, Long todoId) {
    Todo todo =
        todoRepository
            .findById(todoId)
            .orElseThrow(() -> new TodoNotFoundException(TodoErrorCode.TODO_NOT_FOUND));

    if (!todo.getUserId().equals(userId)) {
      throw new TodoUnauthorizedAccessException(TodoErrorCode.UNAUTHORIZED_TODO_ACCESS);
    }

    todoRepository.delete(todo);
    return TodoResponse.builder().todoId(todo.getTodoId()).message("할 일이 삭제되었습니다.").build();
  }
}
