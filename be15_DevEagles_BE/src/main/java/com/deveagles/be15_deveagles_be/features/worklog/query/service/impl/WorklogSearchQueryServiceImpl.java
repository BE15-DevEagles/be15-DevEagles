package com.deveagles.be15_deveagles_be.features.worklog.query.service.impl;

import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.TeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamCommandService;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.FindInfoRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogResponse;
import com.deveagles.be15_deveagles_be.features.worklog.query.mapper.WorklogMapper;
import com.deveagles.be15_deveagles_be.features.worklog.query.service.WorklogSearchQueryService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorklogSearchQueryServiceImpl implements WorklogSearchQueryService {
  private final WorklogMapper worklogMapper;
  private final TeamCommandService teamCommandService;

  @Override
  @Transactional(readOnly = true)
  public List<WorklogResponse> searchWorklogs(FindInfoRequest request) {
    String keyword = request.getKeyword();
    if (keyword == null) keyword = "";
    validateTeamExists(request.getTeamId());
    LocalDateTime startDate = request.getStartDate();
    LocalDateTime endDate = request.getEndDate();

    return worklogMapper.searchWorklogs(
        request.getTeamId(), request.getSearchType().name(), keyword, startDate, endDate);
  }

  public void validateTeamExists(Long teamId) {
    TeamResponse detail = teamCommandService.getTeamDetail(teamId);
    if (detail == null || detail.getTeamId() == null) {
      throw new TeamBusinessException(TeamErrorCode.TEAM_NOT_FOUND);
    }
  }
}
