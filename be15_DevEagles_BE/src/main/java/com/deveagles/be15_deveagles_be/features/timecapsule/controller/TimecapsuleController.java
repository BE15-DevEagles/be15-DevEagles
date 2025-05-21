package com.deveagles.be15_deveagles_be.features.timecapsule.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.timecapsule.dto.TimecapsuleCreateRequest;
import com.deveagles.be15_deveagles_be.features.timecapsule.service.TimecapsuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/timecapsules")
@RequiredArgsConstructor
public class TimecapsuleController {

  private final TimecapsuleService timecapsuleService;

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createTimecapsule(
      @Valid @RequestBody TimecapsuleCreateRequest request) {
    timecapsuleService.createTimecapsule(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
  }
}
