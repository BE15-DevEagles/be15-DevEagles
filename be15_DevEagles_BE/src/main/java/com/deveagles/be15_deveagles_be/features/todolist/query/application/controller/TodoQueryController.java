package com.deveagles.be15_deveagles_be.features.todolist.query.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.common.dto.PagedResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.*;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.service.TodoCalendarQueryService;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.service.TodoDdayQueryService;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.service.TodoTeamQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoQueryController {

  private final TodoCalendarQueryService calendarQueryService;
  private final TodoDdayQueryService ddayQueryService;
  private final TodoTeamQueryService teamQueryService;

  // 1️⃣ 내 캘린더용 일정 조회
  @GetMapping("/calendar/my")
  public ResponseEntity<ApiResponse<List<MyCalendarTodoResponse>>> getMyCalendarTodos(
      @AuthenticationPrincipal CustomUser user) {
    List<MyCalendarTodoResponse> response =
        calendarQueryService.getMyCalendarTodos(user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // 2️⃣ 내 미완료 todo 리스트 (D-Day 포함)
  @GetMapping("/dday/my")
  public ResponseEntity<ApiResponse<PagedResponse<MyDdayTodoResponse>>>
      getMyIncompleteTodosWithDday(
          @AuthenticationPrincipal CustomUser user,
          @RequestParam int page,
          @RequestParam int size) {
    PagedResponse<MyDdayTodoResponse> response =
        ddayQueryService.getMyIncompleteTodosWithDday(user.getUserId(), page, size);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // 3️⃣ 특정 팀의 전체 캘린더 일정 조회
  @GetMapping("/calendar/team/{teamId}")
  public ResponseEntity<ApiResponse<List<TeamCalendarTodoResponse>>> getTeamCalendarTodos(
      @PathVariable Long teamId) {
    List<TeamCalendarTodoResponse> response = calendarQueryService.getTeamCalendarTodos(teamId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // 4️⃣ 특정 팀에서 내가 작성한 미완료 todo 리스트 (D-Day 포함)
  @GetMapping("/dday/team/{teamId}")
  public ResponseEntity<ApiResponse<PagedResponse<MyTeamDdayTodoResponse>>>
      getMyTeamIncompleteTodosWithDday(
          @AuthenticationPrincipal CustomUser user,
          @PathVariable Long teamId,
          @RequestParam int page,
          @RequestParam int size) {
    PagedResponse<MyTeamDdayTodoResponse> response =
        ddayQueryService.getMyTeamIncompleteTodosWithDday(user.getUserId(), teamId, page, size);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // 5️⃣ 특정 팀 todo 리스트 목록 조회
  @GetMapping("/team/{teamId}")
  public ResponseEntity<ApiResponse<PagedResponse<TeamFilteredTodoResponse>>>
      getTeamTodosByCondition(
          @PathVariable Long teamId,
          @RequestParam(required = false) List<Long> userIds,
          @RequestParam(defaultValue = "all") String status,
          @RequestParam int page,
          @RequestParam int size) {

    PagedResponse<TeamFilteredTodoResponse> response =
        teamQueryService.getTeamTodosByCondition(teamId, userIds, status, page, size);

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
