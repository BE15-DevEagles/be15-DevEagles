package com.deveagles.be15_deveagles_be.features.todolist.command.application.service;

import com.deveagles.be15_deveagles_be.features.todolist.command.domain.repository.WorklogQueryRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorklogQueryService {

  private final WorklogQueryRepository worklogQueryRepository;

  public boolean hasWrittenToday(Long userId, Long teamId) {
    return worklogQueryRepository.existsByUserAndTeamAndDate(userId, teamId, LocalDateTime.now());
  }
}
