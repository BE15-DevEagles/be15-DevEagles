package com.deveagles.be15_deveagles_be.features.team.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.CreateTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.CreateTeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamCommandService;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.Team;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMember;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMemberId;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamRepository;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamCommandServiceImpl implements TeamCommandService {

  private final TeamRepository teamRepository;
  private final UserRepository userRepository;
  private final TeamMemberRepository teamMemberRepository;

  @Override
  @Transactional
  public CreateTeamResponse createTeam(Long userId, CreateTeamRequest request) {
    // 1. 팀 이름 중복 검증
    if (teamRepository.existsByTeamName(request.getTeamName())) {
      throw new TeamBusinessException(TeamErrorCode.TEAM_NAME_DUPLICATION);
    }

    // 2. 팀 생성
    Team team =
        Team.builder()
            .userId(userId)
            .teamName(request.getTeamName())
            .introduction(request.getIntroduction())
            .build();

    Team saved = teamRepository.save(team);

    // 3. 팀장을 팀원으로 등록
    User teamLeader =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new TeamBusinessException(
                        TeamErrorCode.USER_NOT_FOUND)); // 보통 존재한다고 가정되지만 안정성 위해 체크

    TeamMember teamMember =
        TeamMember.builder()
            .id(new TeamMemberId(userId, saved.getTeamId()))
            .user(teamLeader)
            .team(saved)
            .build();

    teamMemberRepository.save(teamMember);

    // 4. 응답 반환
    return CreateTeamResponse.builder()
        .teamId(saved.getTeamId())
        .teamName(saved.getTeamName())
        .introduction(saved.getIntroduction())
        .build();
  }
}
