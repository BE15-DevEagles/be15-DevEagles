package com.deveagles.be15_deveagles_be.features.team.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.CreateTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.CreateTeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.impl.TeamCommandServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {

  private final TeamCommandServiceImpl teamCommandServiceimpl;

  @PostMapping
  public ResponseEntity<ApiResponse<CreateTeamResponse>> createTeam(
      // @AuthenticationPrincipal CustomUser customUser,
      @Valid @RequestBody CreateTeamRequest request) {

    Long userId = 1L; // user_id 하드코딩
    //        Long userId = customUser.getUserId();
    CreateTeamResponse response = teamCommandServiceimpl.createTeam(userId, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }
}
