package com.deveagles.be15_deveagles_be.features.team.command.domain.repository;

import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMember;

public interface TeamMemberRepository {
  // 이미 팀에 존재하는 팀원 예외처리를 위한 메서드
  boolean existsByTeamTeamIdAndUserUserId(Long teamId, Long userId);

  // 팀원 등록 메서드
  TeamMember save(TeamMember teamMember);
}
