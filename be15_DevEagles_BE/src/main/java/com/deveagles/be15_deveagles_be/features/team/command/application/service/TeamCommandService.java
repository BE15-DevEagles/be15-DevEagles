package com.deveagles.be15_deveagles_be.features.team.command.application.service;

import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.CreateTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.CreateTeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.TeamResponse;

public interface TeamCommandService {
  CreateTeamResponse createTeam(Long userId, CreateTeamRequest request);

  void deleteTeam(Long userId, Long teamId);

  TeamResponse getTeamDetail(Long teamId);
}
