package com.deveagles.be15_deveagles_be.features.worklog.query.service;

import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.FindInfoRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogResponse;
import java.util.List;

public interface WorklogSearchQueryService {
  List<WorklogResponse> searchWorklogs(FindInfoRequest request);
}
