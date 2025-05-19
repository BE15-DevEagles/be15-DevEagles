package com.deveagles.be15_deveagles_be.features.team.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.CreateTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.CreateTeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamCommandService;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.Team;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamCommandServiceImpl implements TeamCommandService {

  private final TeamRepository teamRepository;

  @Override
  public CreateTeamResponse createTeam(Long userId, CreateTeamRequest request) {
    if (teamRepository.existsByTeamName(request.getTeamName())) {
      throw new TeamBusinessException(TeamErrorCode.TEAM_NAME_DUPLICATION);
    }
    Team team =
        Team.builder()
            .userId(userId)
            .teamName(request.getTeamName())
            .introduction(request.getIntroduction())
            .build();

    Team saved = teamRepository.save(team);

    return CreateTeamResponse.builder()
        .teamId(saved.getTeamId())
        .teamName(saved.getTeamName())
        .introduction(saved.getIntroduction())
        .build();
  }
}
