package com.deveagles.be15_deveagles_be.features.team.command.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.WithdrawTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.impl.TeamMemberCommandServiceImpl;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.Team;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMember;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMemberId;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamRepository;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.UserStatus;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class WithdrawTeamTest {

  private TeamRepository teamRepository;
  private TeamMemberRepository teamMemberRepository;
  private UserRepository userRepository;
  private TeamMemberCommandServiceImpl teamMemberCommandServiceImpl;

  @BeforeEach
  void setUp() {
    teamRepository = mock(TeamRepository.class);
    teamMemberRepository = mock(TeamMemberRepository.class);
    teamMemberCommandServiceImpl =
        new TeamMemberCommandServiceImpl(teamRepository, userRepository, teamMemberRepository);
  }

  @Test
  @DisplayName("팀원이 정상적으로 팀을 탈퇴한다")
  void withdrawTeam_success() {
    // given
    Long teamId = 1L;
    User member = createUser(2L, "member@example.com");

    WithdrawTeamRequest request = new WithdrawTeamRequest();
    ReflectionTestUtils.setField(request, "teamId", teamId);

    Team team = Team.builder().teamId(teamId).userId(999L).build(); // 팀장 id=999

    TeamMember teamMember = TeamMember.builder().team(team).user(member).build();
    setPrivateField(teamMember, "id", new TeamMemberId(member.getUserId(), teamId));

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(teamMemberRepository.findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(
            member.getUserId(), teamId))
        .thenReturn(Optional.of(teamMember));

    // when
    teamMemberCommandServiceImpl.withdrawTeam(member.getUserId(), request);

    // then
    assertThat(teamMember.getDeletedAt()).isNotNull();
    verify(teamRepository).findById(teamId);
    verify(teamMemberRepository)
        .findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(member.getUserId(), teamId);
  }

  @Test
  @DisplayName("팀장이 팀 탈퇴 시도 시 예외 발생")
  void withdrawTeam_shouldThrowException_whenUserIsTeamLeader() {
    // given
    User leader = createUser(1L, "leader@example.com");
    Long teamId = 1L;

    WithdrawTeamRequest request = new WithdrawTeamRequest();
    ReflectionTestUtils.setField(request, "teamId", teamId);

    Team team = Team.builder().teamId(teamId).userId(leader.getUserId()).build();

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

    // when & then
    assertThatThrownBy(() -> teamMemberCommandServiceImpl.withdrawTeam(leader.getUserId(), request))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.TEAM_LEADER_CANNOT_LEAVE.getMessage());

    verify(teamRepository).findById(teamId);
    verify(teamMemberRepository, never()).findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(any(), any());
  }

  @Test
  @DisplayName("팀이 존재하지 않으면 예외 발생")
  void withdrawTeam_shouldThrowException_whenTeamNotFound() {
    // given
    User member = createUser(2L, "member@example.com");
    Long teamId = 999L;

    WithdrawTeamRequest request = new WithdrawTeamRequest();
    ReflectionTestUtils.setField(request, "teamId", teamId);

    when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> teamMemberCommandServiceImpl.withdrawTeam(member.getUserId(), request))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.TEAM_NOT_FOUND.getMessage());

    verify(teamRepository).findById(teamId);
    verify(teamMemberRepository, never()).findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(any(), any());
  }

  @Test
  @DisplayName("팀원이 아닌 사용자가 탈퇴 시도 시 예외 발생")
  void withdrawTeam_shouldThrowException_whenNotTeamMember() {
    // given
    User outsider = createUser(5L, "outsider@example.com");
    Long teamId = 1L;

    WithdrawTeamRequest request = new WithdrawTeamRequest();
    ReflectionTestUtils.setField(request, "teamId", teamId);

    Team team = Team.builder().teamId(teamId).userId(999L).build();

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(teamMemberRepository.findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(
            outsider.getUserId(), teamId))
        .thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
            () -> teamMemberCommandServiceImpl.withdrawTeam(outsider.getUserId(), request))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.NOT_TEAM_MEMBER.getMessage());

    verify(teamRepository).findById(teamId);
    verify(teamMemberRepository)
        .findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(outsider.getUserId(), teamId);
  }

  // 공통 유저 생성 util
  private User createUser(Long userId, String email) {
    User user =
        User.builder()
            .email(email)
            .password("pw")
            .userName("테스트")
            .phoneNumber("01000000000")
            .userStatus(UserStatus.ENABLED)
            .createdAt(LocalDateTime.now())
            .modifiedAt(LocalDateTime.now())
            .deletedAt(null)
            .build();
    setPrivateField(user, "userId", userId);
    return user;
  }

  private void setPrivateField(Object target, String fieldName, Object value) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
