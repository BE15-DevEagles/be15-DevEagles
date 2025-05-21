package com.deveagles.be15_deveagles_be.features.timecapsule.service;

import com.deveagles.be15_deveagles_be.features.timecapsule.dto.TimecapsuleCreateRequest;
import com.deveagles.be15_deveagles_be.features.timecapsule.entity.Timecapsule;
import com.deveagles.be15_deveagles_be.features.timecapsule.repository.TimecapsuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimecapsuleService {

  private final TimecapsuleRepository timecapsuleRepository;

  public Timecapsule createTimecapsule(TimecapsuleCreateRequest request) {
    Timecapsule timecapsule =
        Timecapsule.builder()
            .timecapsuleContent(request.getTimecapsuleContent())
            .openDate(request.getOpenDate())
            .userId(request.getUserId())
            .teamId(request.getTeamId())
            .timecapsuleStatus(Timecapsule.TimecapsuleStatus.ACTIVE)
            .build();
    return timecapsuleRepository.save(timecapsule);
  }
}
