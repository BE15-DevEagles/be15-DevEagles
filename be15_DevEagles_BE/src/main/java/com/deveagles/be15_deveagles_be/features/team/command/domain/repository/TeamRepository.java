package com.deveagles.be15_deveagles_be.features.team.command.domain.repository;

import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.Team;
import java.util.Optional;

public interface TeamRepository {
  // 팀 생성을 위한 메서드
  Team save(Team team);

  // 팀 이름 중복 예외 처리를 위한 메서드
  boolean existsByTeamName(String teamName);

  Optional<Team> findById(Long teamId);
}
