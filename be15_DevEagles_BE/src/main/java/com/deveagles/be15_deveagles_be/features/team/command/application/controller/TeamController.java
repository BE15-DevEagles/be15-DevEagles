package com.deveagles.be15_deveagles_be.features.team.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.CreateTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.FireTeamMemberRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.InviteTeamMemberRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.CreateTeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.impl.TeamCommandServiceImpl;
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

  private final TeamCommandServiceImpl teamCommandServiceimpl;

  // 팀 생성 API
  @PostMapping
  public ResponseEntity<ApiResponse<CreateTeamResponse>> createTeam(
      @AuthenticationPrincipal CustomUser customUser,
      @Valid @RequestBody CreateTeamRequest request) {

    Long userId = customUser.getUserId();
    CreateTeamResponse response = teamCommandServiceimpl.createTeam(userId, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }

  // 팀원 초대 API
  @PostMapping("/{teamId}/members/invite")
  public ResponseEntity<ApiResponse<String>> inviteTeamMember(
      @AuthenticationPrincipal CustomUser customUser,
      @PathVariable Long teamId,
      @Valid @RequestBody InviteTeamMemberRequest request) {

    Long inviterId = customUser.getUserId();

    teamCommandServiceimpl.inviteTeamMember(inviterId, teamId, request.getEmail());

    return ResponseEntity.ok(ApiResponse.success("팀원 초대가 완료되었습니다."));
  }

  // 팀원 추방 API
  @PostMapping("/{teamId}/members/fire")
  public ResponseEntity<ApiResponse<String>> fireTeamMember(
      @AuthenticationPrincipal CustomUser customUser,
      @PathVariable Long teamId,
      @Valid @RequestBody FireTeamMemberRequest request) {

    Long leaderId = customUser.getUserId();

    teamCommandServiceimpl.fireTeamMember(leaderId, teamId, request.getEmail());

    return ResponseEntity.ok(ApiResponse.success("팀원 추방이 완료되었습니다."));
  }
}
