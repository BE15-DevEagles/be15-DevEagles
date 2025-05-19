package com.deveagles.be15_deveagles_be.features.todolist.command.application.service;

import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.CreateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response.TodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.aggregate.Todo;
import com.deveagles.be15_deveagles_be.features.todolist.command.domain.repository.TodoRepository;
import com.deveagles.be15_deveagles_be.features.todolist.exception.InvalidTodoDateException;
import com.deveagles.be15_deveagles_be.features.todolist.exception.TodoErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoService {

  private final TodoRepository todoRepository;

  @Transactional
  public TodoResponse createTodo(CreateTodoRequest request) {
    Long userId = 1L; // 하드코딩

    if (request.getStartDate().isAfter(request.getDueDate())) {
      throw new InvalidTodoDateException(TodoErrorCode.INVALID_TODO_DATE);
    }

    Todo todo =
        Todo.builder()
            .userId(userId)
            .teamId(request.getTeamId())
            .content(request.getContent())
            .startDate(request.getStartDate())
            .dueDate(request.getDueDate())
            .build();

    Todo saved = todoRepository.save(todo);

    return TodoResponse.builder().todoId(saved.getTodoId()).message("할 일이 등록되었습니다.").build();
  }
}
