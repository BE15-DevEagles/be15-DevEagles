package com.deveagles.be15_deveagles_be.features.timecapsule.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.timecapsule.command.application.dto.request.CreateTimecapsuleRequest;
import com.deveagles.be15_deveagles_be.features.timecapsule.command.application.service.TimecapsuleService;
import com.deveagles.be15_deveagles_be.features.timecapsule.command.domain.aggregate.Timecapsule;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/timecapsules")
@RequiredArgsConstructor
public class TimecapsuleController {

  private final TimecapsuleService timecapsuleService;

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createTimecapsule(
      @Valid @RequestBody CreateTimecapsuleRequest request,
      @AuthenticationPrincipal CustomUser user) {
    timecapsuleService.createTimecapsule(request, user.getUserId());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
  }

  @GetMapping("/team/{teamId}/open")
  public ResponseEntity<ApiResponse<List<Timecapsule>>> openTeamTimecapsules(
      @PathVariable Long teamId, @AuthenticationPrincipal CustomUser user) {
    List<Timecapsule> result = timecapsuleService.openTeamTimecapsules(teamId, user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}
