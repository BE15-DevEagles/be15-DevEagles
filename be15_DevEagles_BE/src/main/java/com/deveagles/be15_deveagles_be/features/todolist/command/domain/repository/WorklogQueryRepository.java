package com.deveagles.be15_deveagles_be.features.todolist.command.domain.repository;

import com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate.Worklog;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorklogQueryRepository extends JpaRepository<Worklog, Long> {

  @Query(
      """
    SELECT COUNT(w) > 0
    FROM Worklog w
    WHERE w.userId = :userId
      AND w.teamId = :teamId
      AND FUNCTION('DATE', w.writtenAt) = FUNCTION('DATE', :dateTime)
  """)
  boolean existsByUserAndTeamAndDate(
      @Param("userId") Long userId,
      @Param("teamId") Long teamId,
      @Param("dateTime") LocalDateTime dateTime);
}
