package com.deveagles.be15_deveagles_be.features.team.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.CreateTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.CreateTeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {

  private final TeamCommandService teamCommandService;

  // 팀 생성 API
  @PostMapping
  public ResponseEntity<ApiResponse<CreateTeamResponse>> createTeam(
      @AuthenticationPrincipal CustomUser customUser,
      @Valid @RequestBody CreateTeamRequest request) {

    Long userId = customUser.getUserId();
    CreateTeamResponse response = teamCommandService.createTeam(userId, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }

  @DeleteMapping("/{teamId}")
  public ResponseEntity<ApiResponse<String>> deleteTeam(
      @AuthenticationPrincipal CustomUser customUser, @PathVariable Long teamId) {

    Long userId = customUser.getUserId(); // 현재 로그인한 유저 ID
    teamCommandService.deleteTeam(userId, teamId);

    return ResponseEntity.ok(ApiResponse.success("팀이 삭제되었습니다."));
  }
}
