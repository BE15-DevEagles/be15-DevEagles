package com.deveagles.be15_deveagles_be.features.team.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.FireTeamMemberRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.InviteTeamMemberRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.TransferLeaderRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.WithdrawTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamMemberCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/team/members")
public class TeamMemberController {

  private final TeamMemberCommandService teamMemberCommandService;

  // 팀원 초대 API
  @PostMapping("/{teamId}/invite")
  public ResponseEntity<ApiResponse<String>> inviteTeamMember(
      @AuthenticationPrincipal CustomUser customUser,
      @PathVariable Long teamId,
      @Valid @RequestBody InviteTeamMemberRequest request) {

    Long inviterId = customUser.getUserId();

    teamMemberCommandService.inviteTeamMember(inviterId, teamId, request.getEmail());

    return ResponseEntity.ok(ApiResponse.success("팀원 초대가 완료되었습니다."));
  }

  // 팀원 추방 API
  @PostMapping("/{teamId}/fire")
  public ResponseEntity<ApiResponse<String>> fireTeamMember(
      @AuthenticationPrincipal CustomUser customUser,
      @PathVariable Long teamId,
      @Valid @RequestBody FireTeamMemberRequest request) {

    Long leaderId = customUser.getUserId();

    teamMemberCommandService.fireTeamMember(leaderId, teamId, request.getEmail());

    return ResponseEntity.ok(ApiResponse.success("팀원 추방이 완료되었습니다."));
  }

  // 팀 탈퇴 API
  @PostMapping("/withdraw")
  public ResponseEntity<ApiResponse<String>> withdrawTeam(
      @AuthenticationPrincipal CustomUser customUser,
      @Valid @RequestBody WithdrawTeamRequest request) {

    Long userId = customUser.getUserId();

    teamMemberCommandService.withdrawTeam(userId, request);

    return ResponseEntity.ok(ApiResponse.success("팀 탈퇴가 완료되었습니다."));
  }

  @PatchMapping("/{teamId}/transfer")
  public ResponseEntity<ApiResponse<String>> transferLeadership(
      @AuthenticationPrincipal CustomUser customUser,
      @PathVariable Long teamId,
      @Valid @RequestBody TransferLeaderRequest request) {

    Long currentLeaderId = customUser.getUserId();
    teamMemberCommandService.transferLeadership(currentLeaderId, teamId, request);

    return ResponseEntity.ok(ApiResponse.success("팀장 권한이 성공적으로 양도되었습니다."));
  }
}
