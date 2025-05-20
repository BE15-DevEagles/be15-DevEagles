package com.deveagles.be15_deveagles_be.features.team.command.application.service;

import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.CreateTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.CreateTeamResponse;

public interface TeamCommandService {
  CreateTeamResponse createTeam(Long userId, CreateTeamRequest request);

  void inviteTeamMember(Long inviterId, Long teamId, String email);

  void fireTeamMember(Long userId, Long teamId, String email);
}
