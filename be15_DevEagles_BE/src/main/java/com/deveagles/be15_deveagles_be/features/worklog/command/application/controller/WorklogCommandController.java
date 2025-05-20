package com.deveagles.be15_deveagles_be.features.worklog.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.WorklogCreateRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogDetailResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.service.WorklogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/worklog")
@RequiredArgsConstructor
public class WorklogCommandController {
  private final WorklogService worklogService;

  /*업무일지 등록*/
  @PostMapping("/register/{teamId}")
  public ResponseEntity<ApiResponse<WorklogDetailResponse>> registerWorklog(
      @RequestBody WorklogCreateRequest worklogCreateRequest,
      @AuthenticationPrincipal CustomUser customUser,
      @PathVariable Long teamId) {
    Long userId = customUser.getUserId();

    WorklogDetailResponse response =
        worklogService.createWorklog(userId, teamId, worklogCreateRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }

  /*    */
  /*업무일지 조회*/
  /*
  @GetMapping
  public ResponseEntity<ApiResponse<List<WorklogResponse>>> worklogList(){
      List<WorklogResponse> response = worklogService.findAllWorklog();
      return ResponseEntity.ok(ApiResponse.success(response));
  }
  */
  /*업무일지 상세 조회*/
  /*
  @GetMapping("{worklogId}")
  public ResponseEntity<ApiResponse<WorklogDetailResponse>> worklogDetail(
          @PathVariable("worklogId") Long worklogId) {

      return null;

  }*/
}
