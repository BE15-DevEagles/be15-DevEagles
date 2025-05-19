package com.deveagles.be15_deveagles_be.features.todolist.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.CreateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.UpdateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response.TodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.service.TodoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {

  private final TodoService todoService;

  // ✅ 작성 (다건)
  @PostMapping
  public ResponseEntity<ApiResponse<List<TodoResponse>>> createTodos(
      @RequestBody List<CreateTodoRequest> requests) {
    List<TodoResponse> response = todoService.createTodos(requests);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // ✅ 완료
  @PutMapping("/{todoId}/complete")
  public ResponseEntity<ApiResponse<TodoResponse>> completeTodo(@PathVariable Long todoId) {
    TodoResponse response = todoService.completeTodo(todoId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // ✅ 수정
  @PutMapping("/{todoId}")
  public ResponseEntity<ApiResponse<TodoResponse>> updateTodo(
      @PathVariable Long todoId, @RequestBody UpdateTodoRequest request) {
    TodoResponse response = todoService.updateTodo(todoId, request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // ✅ 삭제
  @DeleteMapping("/{todoId}")
  public ResponseEntity<ApiResponse<TodoResponse>> deleteTodo(@PathVariable Long todoId) {
    TodoResponse response = todoService.deleteTodo(todoId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
