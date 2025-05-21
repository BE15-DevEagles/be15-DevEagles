package com.deveagles.be15_deveagles_be.features.todolist.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.CreateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.request.UpdateTodoRequest;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response.TodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response.WorklogWrittenCheckResponse;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.service.TodoService;
import com.deveagles.be15_deveagles_be.features.todolist.command.application.service.WorklogQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {

  private final TodoService todoService;
  private final WorklogQueryService worklogQueryService;

  @PostMapping
  public ResponseEntity<ApiResponse<List<TodoResponse>>> createTodos(
      @AuthenticationPrincipal CustomUser user, @RequestBody List<CreateTodoRequest> requests) {
    List<TodoResponse> response = todoService.createTodos(user.getUserId(), requests);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PutMapping("/{todoId}/complete")
  public ResponseEntity<ApiResponse<TodoResponse>> completeTodo(
      @AuthenticationPrincipal CustomUser user, @PathVariable Long todoId) {
    TodoResponse response = todoService.completeTodo(user.getUserId(), todoId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PutMapping("/{todoId}")
  public ResponseEntity<ApiResponse<TodoResponse>> updateTodo(
      @AuthenticationPrincipal CustomUser user,
      @PathVariable Long todoId,
      @RequestBody UpdateTodoRequest request) {
    TodoResponse response = todoService.updateTodo(user.getUserId(), todoId, request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @DeleteMapping("/{todoId}")
  public ResponseEntity<ApiResponse<TodoResponse>> deleteTodo(
      @AuthenticationPrincipal CustomUser user, @PathVariable Long todoId) {
    TodoResponse response = todoService.deleteTodo(user.getUserId(), todoId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/worklog/written")
  public ResponseEntity<ApiResponse<WorklogWrittenCheckResponse>> checkWorklogWrittenToday(
      @AuthenticationPrincipal CustomUser user, @RequestParam Long teamId) {

    boolean written = worklogQueryService.hasWrittenToday(user.getUserId(), teamId);
    return ResponseEntity.ok(ApiResponse.success(WorklogWrittenCheckResponse.of(written)));
  }
}
