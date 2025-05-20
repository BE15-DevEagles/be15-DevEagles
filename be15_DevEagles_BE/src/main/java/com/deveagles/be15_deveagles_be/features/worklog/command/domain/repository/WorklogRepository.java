package com.deveagles.be15_deveagles_be.features.worklog.command.domain.repository;

import com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate.Worklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorklogRepository extends JpaRepository<Worklog, Long> {}
