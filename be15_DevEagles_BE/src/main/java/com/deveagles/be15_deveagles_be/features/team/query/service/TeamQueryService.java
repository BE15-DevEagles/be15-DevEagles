package com.deveagles.be15_deveagles_be.features.team.query.service;

import com.deveagles.be15_deveagles_be.features.team.query.dto.response.MyTeamListResponse;
import java.util.List;

public interface TeamQueryService {

  List<MyTeamListResponse> getTeamsByUserId(Long userId);
}
