package com.deveagles.be15_deveagles_be.features.timecapsule.controller;

import com.deveagles.be15_deveagles_be.features.timecapsule.dto.TimecapsuleCreateRequest;
import com.deveagles.be15_deveagles_be.features.timecapsule.entity.Timecapsule;
import com.deveagles.be15_deveagles_be.features.timecapsule.service.TimecapsuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/timecapsules")
@RequiredArgsConstructor
public class TimecapsuleController {

  private final TimecapsuleService timecapsuleService;

  @PostMapping
  public ResponseEntity<Timecapsule> createTimecapsule(
      @Valid @RequestBody TimecapsuleCreateRequest request) {
    Timecapsule created = timecapsuleService.createTimecapsule(request);
    return ResponseEntity.ok(created);
  }
}
