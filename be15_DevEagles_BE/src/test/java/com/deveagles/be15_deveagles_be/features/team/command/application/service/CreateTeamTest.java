package com.deveagles.be15_deveagles_be.features.team.command.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.CreateTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.CreateTeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.impl.TeamCommandServiceImpl;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.Team;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamRepository;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CreateTeamTest {

  private TeamRepository teamRepository;
  private UserRepository userRepository;
  private TeamMemberRepository teamMemberRepository;
  private TeamCommandServiceImpl teamCommandServiceImpl;

  @BeforeEach
  void setUp() {
    teamRepository = mock(TeamRepository.class);
    userRepository = mock(UserRepository.class); // ✅ 추가
    teamMemberRepository = mock(TeamMemberRepository.class); // ✅ 추가
    teamCommandServiceImpl =
        new TeamCommandServiceImpl(teamRepository, userRepository, teamMemberRepository);
  }

  @Test
  @DisplayName("팀 생성 성공 테스트")
  void createTeam() {
    // given
    Long userId = 1L;

    CreateTeamRequest request = new CreateTeamRequest();
    ReflectionTestUtils.setField(request, "teamName", "DevEagles");
    ReflectionTestUtils.setField(request, "introduction", "한화의 연승을 기원합니다.");

    when(teamRepository.existsByTeamName("DevEagles")).thenReturn(false);

    Team team =
        Team.builder().userId(userId).teamName("DevEagles").introduction("한화의 연승을 기원합니다.").build();
    ReflectionTestUtils.setField(team, "teamId", 100L);

    when(teamRepository.save(any(Team.class))).thenReturn(team);

    // 팀장 유저 조회 Mock
    User user = mock(User.class);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // when
    CreateTeamResponse response = teamCommandServiceImpl.createTeam(userId, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getTeamId()).isEqualTo(100L);
    assertThat(response.getTeamName()).isEqualTo("DevEagles");
    assertThat(response.getIntroduction()).isEqualTo("한화의 연승을 기원합니다.");

    verify(teamRepository).existsByTeamName("DevEagles");
    verify(teamRepository).save(any(Team.class));

    // ✅ 팀장도 팀원으로 저장되었는지 확인
    verify(userRepository).findById(userId);
    verify(teamMemberRepository).save(any());
  }

  @Test
  @DisplayName("팀 생성 중복 예외처리 테스트")
  void createTeam_shouldThrowException_whenTeamNameAlreadyExists() {
    // given
    CreateTeamRequest request = new CreateTeamRequest();
    ReflectionTestUtils.setField(request, "teamName", "중복팀");
    ReflectionTestUtils.setField(request, "introduction", "소개입니다");

    when(teamRepository.existsByTeamName("중복팀")).thenReturn(true);

    // when & then
    assertThatThrownBy(() -> teamCommandServiceImpl.createTeam(1L, request))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.TEAM_NAME_DUPLICATION.getMessage());

    verify(teamRepository).existsByTeamName("중복팀");
    verify(teamRepository, never()).save(any());
    verify(teamMemberRepository, never()).save(any()); // ✅ 팀원이 추가되지 않아야 함
  }
}
