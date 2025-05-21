package com.deveagles.be15_deveagles_be.features.todolist.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.deveagles.be15_deveagles_be.features.todolist.command.domain.repository.WorklogQueryRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorklogQueryServiceTest {

  @Mock private WorklogQueryRepository worklogQueryRepository;

  @InjectMocks private WorklogQueryService worklogQueryService;

  @Test
  @DisplayName("오늘 작업일지 작성 여부 확인 - 작성된 경우")
  void hasWrittenToday_true() {
    Long userId = 1L;
    Long teamId = 2L;

    when(worklogQueryRepository.existsByUserAndTeamAndDate(
            eq(userId), eq(teamId), any(LocalDateTime.class)))
        .thenReturn(true);

    boolean result = worklogQueryService.hasWrittenToday(userId, teamId);

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("오늘 작업일지 작성 여부 확인 - 작성되지 않은 경우")
  void hasWrittenToday_false() {
    Long userId = 1L;
    Long teamId = 2L;

    when(worklogQueryRepository.existsByUserAndTeamAndDate(
            eq(userId), eq(teamId), any(LocalDateTime.class)))
        .thenReturn(false);

    boolean result = worklogQueryService.hasWrittenToday(userId, teamId);

    assertThat(result).isFalse();
  }
}
