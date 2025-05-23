package com.deveagles.be15_deveagles_be.features.timecapsule.command.repository;

import com.deveagles.be15_deveagles_be.features.timecapsule.command.domain.aggregate.Timecapsule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimecapsuleRepository extends JpaRepository<Timecapsule, Long> {
  List<Timecapsule>
      findByTeamIdAndUserIdAndOpenDateLessThanEqualAndTimecapsuleStatusAndOpenedAtIsNull(
          Long teamId, Long userId, LocalDate today, Timecapsule.TimecapsuleStatus status);
}
