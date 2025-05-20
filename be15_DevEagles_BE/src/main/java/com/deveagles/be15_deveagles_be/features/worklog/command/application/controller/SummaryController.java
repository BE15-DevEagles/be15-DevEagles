package com.deveagles.be15_deveagles_be.features.worklog.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.WorkSummaryRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.SummaryResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.service.WorklogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/summary")
@RestController
@RequiredArgsConstructor
public class SummaryController {
  private final WorklogService worklogService;

  @PostMapping
  public ResponseEntity<ApiResponse<SummaryResponse>> getSummaryFromAiChat(
      @RequestBody WorkSummaryRequest workSummaryRequest) {
    return ResponseEntity.ok(
        ApiResponse.success(worklogService.summaryGenerate(workSummaryRequest)));
  }
}
