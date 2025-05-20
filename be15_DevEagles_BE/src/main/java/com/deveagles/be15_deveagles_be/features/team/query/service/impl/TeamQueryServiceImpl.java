package com.deveagles.be15_deveagles_be.features.team.query.service.impl;

import com.deveagles.be15_deveagles_be.features.team.query.dto.response.MyTeamListResponse;
import com.deveagles.be15_deveagles_be.features.team.query.mapper.TeamMapper;
import com.deveagles.be15_deveagles_be.features.team.query.service.TeamQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamQueryServiceImpl implements TeamQueryService {

  private final TeamMapper teamMapper;

  @Override
  public List<MyTeamListResponse> getTeamsByUserId(Long userId) {
    return teamMapper.selectMyTeamList(userId);
  }
}
