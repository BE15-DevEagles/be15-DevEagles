package com.deveagles.be15_deveagles_be.features.timecapsule.command.repository;

import com.deveagles.be15_deveagles_be.features.timecapsule.command.domain.aggregate.Timecapsule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimecapsuleRepository extends JpaRepository<Timecapsule, Long> {}
