package com.deveagles.be15_deveagles_be.features.team.query.service.impl;

import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.MyTeamListResponse;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.TeamInformationResponse;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.TeamMemberResponse;
import com.deveagles.be15_deveagles_be.features.team.query.mapper.TeamMapper;
import com.deveagles.be15_deveagles_be.features.team.query.service.TeamQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamQueryServiceImpl implements TeamQueryService {

  private final TeamMapper teamMapper;
  private final TeamMemberRepository teamMemberRepository;

  @Override
  public List<MyTeamListResponse> getTeamsByUserId(Long userId) {
    return teamMapper.selectMyTeamList(userId);
  }

  @Override
  public List<TeamMemberResponse> getTeamMembers(Long userId, Long teamId) {
    // 1. 요청자 유효성 검증 (soft delete된 팀원이면 예외)
    boolean isMember =
        teamMemberRepository
            .findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(userId, teamId)
            .isPresent();

    if (!isMember) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_MEMBER);
    }

    // 2. 팀원 목록 조회 (soft delete된 팀원 제외)
    return teamMapper.selectTeamMembers(teamId);
  }

  @Override
  public TeamInformationResponse getTeamInformation(Long userId, Long teamId) {

    // 1. 요청자 유효성 검증 (soft delete된 팀원이면 예외)
    boolean isMember =
        teamMemberRepository
            .findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(userId, teamId)
            .isPresent();

    if (!isMember) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_MEMBER);
    }

    return teamMapper.selectTeamInformation(teamId);
  }
}
