package com.deveagles.be15_deveagles_be.features.worklog.command.application.service;

import com.deveagles.be15_deveagles_be.common.dto.PagedResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.SearchWorklogRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.WorkSummaryRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.WorklogCreateRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.SummaryResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogDetailResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogResponse;

public interface WorklogService {
  /*업무일지 등록 기능*/
  WorklogDetailResponse createWorklog(
      Long userId, Long teamId, WorklogCreateRequest worklogCreateRequest);

  PagedResponse<WorklogResponse> findMyWorklog(Long userId, SearchWorklogRequest reqeust);
  SummaryResponse summaryGenerate(Long userId, WorkSummaryRequest request);

}
