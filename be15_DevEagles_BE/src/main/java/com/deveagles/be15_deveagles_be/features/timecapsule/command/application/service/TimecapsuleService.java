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
    // 팀이 선택되지 않은 경우 (teamId가 null)
    if (request.getTeamId() == null) {
      throw new IllegalArgumentException("팀을 선택해야 타임캡슐을 생성할 수 있습니다.");
    }

    // 생성 날짜가 오늘 이후가 아닌 경우 (오늘 또는 과거)
    LocalDate today = LocalDate.now();
    if (request.getOpenDate() == null || !request.getOpenDate().isAfter(today)) {
      throw new IllegalArgumentException("타임캡슐 오픈 날짜는 반드시 오늘 이후여야 합니다.");
    }

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
