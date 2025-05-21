package com.deveagles.be15_deveagles_be.features.team.query.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.MyTeamListResponse;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.TeamInformationResponse;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.TeamMemberResponse;
import com.deveagles.be15_deveagles_be.features.team.query.service.TeamQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamQueryController {

  private final TeamQueryService teamQueryService;

  @GetMapping("/my")
  public ResponseEntity<ApiResponse<List<MyTeamListResponse>>> getMyTeamList(
      @AuthenticationPrincipal CustomUser customUser) {
    Long userId = customUser.getUserId();
    List<MyTeamListResponse> teams = teamQueryService.getTeamsByUserId(userId);
    return ResponseEntity.ok(ApiResponse.success(teams));
  }

  @GetMapping("/{teamId}/members")
  public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getTeamMembers(
      @AuthenticationPrincipal CustomUser customUser, @PathVariable Long teamId) {

    Long userId = customUser.getUserId(); // 로그인한 사용자 ID

    List<TeamMemberResponse> members = teamQueryService.getTeamMembers(userId, teamId);
    return ResponseEntity.ok(ApiResponse.success(members));
  }

  @GetMapping("/teams/{teamId}")
  public ResponseEntity<ApiResponse<TeamInformationResponse>> getTeamInformation(
      @AuthenticationPrincipal CustomUser customUser, @PathVariable Long teamId) {

    Long userId = customUser.getUserId(); // 로그인한 사용자 ID

    TeamInformationResponse teamInfo = teamQueryService.getTeamInformation(userId, teamId);
    return ResponseEntity.ok(ApiResponse.success(teamInfo));
  }
}
