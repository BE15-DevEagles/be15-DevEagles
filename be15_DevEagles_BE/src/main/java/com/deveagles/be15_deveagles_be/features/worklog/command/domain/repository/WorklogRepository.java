package com.deveagles.be15_deveagles_be.features.worklog.command.domain.repository;

import com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate.Worklog;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorklogRepository extends JpaRepository<Worklog, Long> {
  boolean existsByTeamIdAndWrittenAt(Long teamId, LocalDateTime writtenAt);

  Page<Worklog> findByUserIdAndTeamId(Long userId, Long teamId, Pageable pageable);

  Page<Worklog> findByTeamId(Long teamId, Pageable pageable);
}
