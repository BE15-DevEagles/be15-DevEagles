package com.deveagles.be15_deveagles_be.features.team.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.CreateTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.CreateTeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
@Tag(name = "팀", description = "팀 관련 API")
public class TeamController {

  private final TeamCommandService teamCommandService;

  // 팀 생성 API
  @Operation(summary = "팀 생성", description = "새로운 팀을 생성합니다.")
  @PostMapping
  public ResponseEntity<ApiResponse<CreateTeamResponse>> createTeam(
      @AuthenticationPrincipal CustomUser customUser,
      @Valid @RequestBody CreateTeamRequest request) {

    Long userId = customUser.getUserId();
    CreateTeamResponse response = teamCommandService.createTeam(userId, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }

  @Operation(summary = "팀 삭제", description = "기존에 존재하던 팀을 삭제합니다.")
  @DeleteMapping("/{teamId}")
  public ResponseEntity<ApiResponse<String>> deleteTeam(
      @AuthenticationPrincipal CustomUser customUser, @PathVariable Long teamId) {

    Long userId = customUser.getUserId(); // 현재 로그인한 유저 ID
    teamCommandService.deleteTeam(userId, teamId);

    return ResponseEntity.ok(ApiResponse.success("팀이 삭제되었습니다."));
  }

  @Operation(summary = "팀 썸네일 변경", description = "팀의 썸네일 사진을 변경합니다.")
  @PostMapping("/teams/{teamId}/thumbnail")
  public ResponseEntity<ApiResponse<String>> uploadTeamThumbnail(
      @AuthenticationPrincipal CustomUser customUser,
      @PathVariable Long teamId,
      @RequestParam("file") MultipartFile file)
      throws IOException {

    String url = teamCommandService.uploadTeamThumbnail(customUser.getUserId(), teamId, file);
    return ResponseEntity.ok(ApiResponse.success(url));
  }
}
