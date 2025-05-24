package com.deveagles.be15_deveagles_be.features.team.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.FireTeamMemberRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.InviteTeamMemberRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.TransferLeaderRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.WithdrawTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamMemberCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/team/members")
@Tag(name = "팀원", description = "팀원 관련 API")
public class TeamMemberController {

  private final TeamMemberCommandService teamMemberCommandService;

  // 팀원 초대 API
  @Operation(summary = "팀원 초대", description = "팀원을 팀에 초대합니다.")
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
  @Operation(summary = "팀원 추방", description = "팀원을 팀에서 추방합니다.")
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
  @Operation(summary = "팀 탈퇴", description = "기존의 팀에서 탈퇴합니다.")
  @PostMapping("/withdraw")
  public ResponseEntity<ApiResponse<String>> withdrawTeam(
      @AuthenticationPrincipal CustomUser customUser,
      @Valid @RequestBody WithdrawTeamRequest request) {

    Long userId = customUser.getUserId();

    teamMemberCommandService.withdrawTeam(userId, request);

    return ResponseEntity.ok(ApiResponse.success("팀 탈퇴가 완료되었습니다."));
  }

  @Operation(summary = "팀장 권한 양도", description = "팀원에게 팀장의 권한을 양도합니다.")
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
