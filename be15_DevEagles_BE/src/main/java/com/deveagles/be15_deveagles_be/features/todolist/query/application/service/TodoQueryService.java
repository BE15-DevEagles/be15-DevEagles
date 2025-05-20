package com.deveagles.be15_deveagles_be.features.todolist.query.application.service;

import com.deveagles.be15_deveagles_be.features.todolist.exception.TodoErrorCode;
import com.deveagles.be15_deveagles_be.features.todolist.exception.TodoNotFoundException;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.TodoDetailResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.domain.repository.TodoQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodoQueryService {

  private final TodoQueryRepository todoQueryRepository;

  public TodoDetailResponse getMyTodoDetail(Long userId, Long todoId) {
    TodoDetailResponse result = todoQueryRepository.selectTodoDetail(todoId, userId);
    if (result == null) {
      throw new TodoNotFoundException(TodoErrorCode.TODO_NOT_FOUND);
    }
    return result;
  }
}
