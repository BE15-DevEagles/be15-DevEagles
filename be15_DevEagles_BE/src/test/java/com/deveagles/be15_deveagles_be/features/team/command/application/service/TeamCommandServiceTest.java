package com.deveagles.be15_deveagles_be.features.team.command.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.CreateTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.CreateTeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.impl.TeamCommandServiceImpl;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.Team;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TeamCommandServiceTest {

  private TeamRepository teamRepository;
  private TeamCommandServiceImpl teamCommandServiceImpl;

  // 의존성 주입
  @BeforeEach
  void setUp() {
    teamRepository = mock(TeamRepository.class); // 가짜 TeamRepository 생성
    teamCommandServiceImpl = new TeamCommandServiceImpl(teamRepository); // 데이터 주입
  }

  @Test
  @DisplayName("팀 생성 성공 테스트")
  void createTeam() {
    // given
    Long userId = 1L;
    CreateTeamRequest request = new CreateTeamRequest("DevEagles", "한화의 연승을 기원합니다.");

    when(teamRepository.existsByTeamName("DevEagles")).thenReturn(false);

    Team team =
        Team.builder().userId(userId).teamName("DevEagles").introduction("한화의 연승을 기원합니다.").build();

    ReflectionTestUtils.setField(team, "teamId", 100L);

    when(teamRepository.save(any(Team.class))).thenReturn(team);

    // when
    CreateTeamResponse response = teamCommandServiceImpl.createTeam(userId, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getTeamId()).isEqualTo(100L);
    assertThat(response.getTeamName()).isEqualTo("DevEagles");
    assertThat(response.getIntroduction()).isEqualTo("한화의 연승을 기원합니다.");

    verify(teamRepository).existsByTeamName("DevEagles");
    verify(teamRepository).save(any(Team.class));
  }

  @Test
  @DisplayName("팀 생성 중복 예외처리 테스트")
  void createTeam_shouldThrowException_whenTeamNameAlreadyExists() {
    // given
    CreateTeamRequest request = new CreateTeamRequest("중복팀", "소개입니다");
    when(teamRepository.existsByTeamName("중복팀")).thenReturn(true);

    // when & then
    assertThatThrownBy(() -> teamCommandServiceImpl.createTeam(1L, request))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.TEAM_NAME_DUPLICATION.getMessage());

    verify(teamRepository).existsByTeamName("중복팀");
    verify(teamRepository, never()).save(any());
  }
}
