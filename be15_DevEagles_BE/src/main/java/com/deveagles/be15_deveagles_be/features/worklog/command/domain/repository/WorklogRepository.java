package com.deveagles.be15_deveagles_be.features.worklog.command.domain.repository;

import com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate.Worklog;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorklogRepository extends JpaRepository<Worklog, Long> {
  @Query(
      "SELECT COUNT(w) > 0 FROM Worklog w WHERE FUNCTION('DATE', w.writtenAt) = FUNCTION('DATE', :dateTime)")
  boolean existsByWorkDateOnly(@Param("dateTime") LocalDateTime dateTime);
  Page<Worklog> findByUserIdAndTeamId(Long userId, Long teamId, Pageable pageable);

}
