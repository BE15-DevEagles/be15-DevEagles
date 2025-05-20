package com.deveagles.be15_deveagles_be.features.worklog.command.application.service;

import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.WorkSummaryRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.SummaryResponse;

public interface WorklogService {
  SummaryResponse summaryGenerate(WorkSummaryRequest request);
}
