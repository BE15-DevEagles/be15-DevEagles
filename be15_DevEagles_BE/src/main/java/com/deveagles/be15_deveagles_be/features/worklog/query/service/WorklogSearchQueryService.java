package com.deveagles.be15_deveagles_be.features.worklog.query.service;

import com.deveagles.be15_deveagles_be.common.dto.PagedResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.FindInfoRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogResponse;

public interface WorklogSearchQueryService {
  PagedResponse<WorklogResponse> searchWorklogs(FindInfoRequest request);
}
