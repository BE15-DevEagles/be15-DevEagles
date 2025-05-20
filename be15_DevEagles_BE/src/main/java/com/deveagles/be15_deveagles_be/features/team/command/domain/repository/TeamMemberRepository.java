package com.deveagles.be15_deveagles_be.features.team.command.domain.repository;

import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMember;
import java.util.Optional;

public interface TeamMemberRepository {
  // 이미 팀에 존재하는 팀원 예외처리를 위한 메서드
  boolean existsByTeamTeamIdAndUserUserId(Long teamId, Long userId);

  // 팀원 등록 메서드
  TeamMember save(TeamMember teamMember);

  // 팀원 엔티티 조회
  Optional<TeamMember> findByTeamTeamIdAndUserUserId(Long teamId, Long userId);
}
