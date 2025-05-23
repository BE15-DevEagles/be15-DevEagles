package com.deveagles.be15_deveagles_be.features.timecapsule.command.application.service;

import com.deveagles.be15_deveagles_be.features.timecapsule.command.application.dto.request.CreateTimecapsuleRequest;
import com.deveagles.be15_deveagles_be.features.timecapsule.command.domain.aggregate.Timecapsule;
import com.deveagles.be15_deveagles_be.features.timecapsule.command.repository.TimecapsuleRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimecapsuleService {

  private final TimecapsuleRepository timecapsuleRepository;

  public void createTimecapsule(CreateTimecapsuleRequest request, Long userId) {
    Timecapsule timecapsule =
        Timecapsule.builder()
            .timecapsuleContent(request.getTimecapsuleContent())
            .openDate(request.getOpenDate())
            .userId(userId)
            .teamId(request.getTeamId())
            .timecapsuleStatus(Timecapsule.TimecapsuleStatus.ACTIVE)
            .build();
    timecapsuleRepository.save(timecapsule);
  }

  @Transactional
  public List<Timecapsule> openTeamTimecapsules(Long teamId, Long userId) {
    LocalDate today = LocalDate.now();
    List<Timecapsule> capsules =
        timecapsuleRepository
            .findByTeamIdAndUserIdAndOpenDateLessThanEqualAndTimecapsuleStatusAndOpenedAtIsNull(
                teamId, userId, today, Timecapsule.TimecapsuleStatus.ACTIVE);

    for (Timecapsule capsule : capsules) {
      capsule.setOpenedAt(LocalDateTime.now());
      capsule.setTimecapsuleStatus(Timecapsule.TimecapsuleStatus.INACTIVE);
    }
    return capsules;
  }
}
