package com.deveagles.be15_deveagles_be.features.worklog.query.service.impl;

import com.deveagles.be15_deveagles_be.common.dto.PagedResponse;
import com.deveagles.be15_deveagles_be.common.dto.Pagination;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.TeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamCommandService;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.FindInfoRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate.SearchType;
import com.deveagles.be15_deveagles_be.features.worklog.query.mapper.WorklogMapper;
import com.deveagles.be15_deveagles_be.features.worklog.query.service.WorklogSearchQueryService;
import java.util.List;
import java.util.Optional;
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
  public PagedResponse<WorklogResponse> searchWorklogs(FindInfoRequest request) {
    // 기본값 처리
    String keyword = Optional.ofNullable(request.getKeyword()).orElse("");
    SearchType searchType = Optional.ofNullable(request.getSearchType()).orElse(SearchType.ALL);
    int page = request.getPage();
    int size = request.getSize();
    int offset = (page - 1) * size;

    // 결과 조회
    List<WorklogResponse> content =
        worklogMapper.searchWorklogs(
            request.getTeamId(),
            searchType.name(),
            keyword,
            request.getStartDate(),
            request.getEndDate(),
            offset,
            size);

    // 총 개수 조회
    int totalCount =
        worklogMapper.countWorklogs(
            request.getTeamId(),
            searchType.name(),
            keyword,
            request.getStartDate(),
            request.getEndDate());

    // 페이징 응답 구성
    Pagination pagination = new Pagination(page, size, totalCount);
    return new PagedResponse<>(content, pagination);
  }

  public void validateTeamExists(Long teamId) {
    TeamResponse detail = teamCommandService.getTeamDetail(teamId);
    if (detail == null || detail.getTeamId() == null) {
      throw new TeamBusinessException(TeamErrorCode.TEAM_NOT_FOUND);
    }
  }
}
