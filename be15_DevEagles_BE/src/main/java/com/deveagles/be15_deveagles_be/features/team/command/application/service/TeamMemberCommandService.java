package com.deveagles.be15_deveagles_be.features.team.command.application.service;

import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.TransferLeaderRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.WithdrawTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.TeamMemberResponse;

public interface TeamMemberCommandService {

  void inviteTeamMember(Long inviterId, Long teamId, String email);

  void fireTeamMember(Long userId, Long teamId, String email);

  void withdrawTeam(Long userId, WithdrawTeamRequest request);

  void transferLeadership(Long currentLeaderId, Long teamId, TransferLeaderRequest request);

  TeamMemberResponse findTeamMember(Long userId, Long teamId);
}
