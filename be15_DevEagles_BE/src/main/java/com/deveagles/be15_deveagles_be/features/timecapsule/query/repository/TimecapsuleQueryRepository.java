package com.deveagles.be15_deveagles_be.features.timecapsule.query.repository;

import com.deveagles.be15_deveagles_be.features.timecapsule.command.domain.aggregate.Timecapsule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimecapsuleQueryRepository extends JpaRepository<Timecapsule, Long> {
  List<Timecapsule> findByUserIdAndTimecapsuleStatus(
      Long userId, Timecapsule.TimecapsuleStatus status);
}
