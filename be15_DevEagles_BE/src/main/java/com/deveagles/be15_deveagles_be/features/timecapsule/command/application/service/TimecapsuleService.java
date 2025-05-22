package com.deveagles.be15_deveagles_be.features.timecapsule.command.application.service;

import com.deveagles.be15_deveagles_be.features.timecapsule.command.application.dto.request.CreateTimecapsuleRequest;
import com.deveagles.be15_deveagles_be.features.timecapsule.command.domain.aggregate.Timecapsule;
import com.deveagles.be15_deveagles_be.features.timecapsule.command.repository.TimecapsuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimecapsuleService {

  private final TimecapsuleRepository repository;

  public void createTimecapsule(CreateTimecapsuleRequest request, Long userId) {
    Timecapsule timecapsule =
        Timecapsule.builder()
            .timecapsuleContent(request.getTimecapsuleContent())
            .openDate(request.getOpenDate())
            .userId(userId)
            .teamId(request.getTeamId())
            .timecapsuleStatus(Timecapsule.TimecapsuleStatus.ACTIVE)
            .build();
    repository.save(timecapsule);
  }
}
