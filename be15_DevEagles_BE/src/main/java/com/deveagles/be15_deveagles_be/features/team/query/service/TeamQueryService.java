package com.deveagles.be15_deveagles_be.features.team.query.service;

import com.deveagles.be15_deveagles_be.features.team.query.dto.response.MyTeamListResponse;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.TeamInformationResponse;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.TeamMemberResponse;
import java.util.List;

public interface TeamQueryService {

  List<MyTeamListResponse> getTeamsByUserId(Long userId);

  List<TeamMemberResponse> getTeamMembers(Long userId, Long teamId);

  TeamInformationResponse getTeamInformation(Long userId, Long teamId);
}
