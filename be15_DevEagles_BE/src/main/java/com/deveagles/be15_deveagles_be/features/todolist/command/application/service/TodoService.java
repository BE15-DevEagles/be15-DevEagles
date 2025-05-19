package com.deveagles.be15_deveagles_be.features.todolist.command.application.service;

import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.CreateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.UpdateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response.TodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.aggregate.Todo;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.repository.TodoRepository;
import com.deveagles.be15_deveagles_be.features.todolist.exception.InvalidTodoDateException;
import com.deveagles.be15_deveagles_be.features.todolist.exception.TodoErrorCode;
import com.deveagles.be15_deveagles_be.features.todolist.exception.TodoNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoService {

  private final TodoRepository todoRepository;

  @Transactional
  public List<TodoResponse> createTodos(List<CreateTodoRequest> requests) {
    Long userId = 1L; // 하드코딩

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

  @Transactional
  public TodoResponse completeTodo(Long todoId) {
    Todo todo =
        todoRepository
            .findById(todoId)
            .orElseThrow(() -> new TodoNotFoundException(TodoErrorCode.TODO_NOT_FOUND));

    todo.complete();

    return TodoResponse.builder().todoId(todo.getTodoId()).message("할 일이 완료 처리되었습니다.").build();
  }

  @Transactional
  public TodoResponse updateTodo(Long todoId, UpdateTodoRequest request) {
    Todo todo =
        todoRepository
            .findById(todoId)
            .orElseThrow(() -> new TodoNotFoundException(TodoErrorCode.TODO_NOT_FOUND));

    if (request.getStartDate().isAfter(request.getDueDate())) {
      throw new InvalidTodoDateException(TodoErrorCode.INVALID_TODO_DATE);
    }

    todo.updateContent(request.getContent(), request.getStartDate(), request.getDueDate());

    return TodoResponse.builder().todoId(todo.getTodoId()).message("할 일이 수정되었습니다.").build();
  }
}
