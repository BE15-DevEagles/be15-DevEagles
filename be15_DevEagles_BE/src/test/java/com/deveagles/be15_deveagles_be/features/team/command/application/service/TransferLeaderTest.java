package com.deveagles.be15_deveagles_be.features.team.command.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.TransferLeaderRequest;
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

class TransferLeadershipTest {

  private TeamRepository teamRepository;
  private UserRepository userRepository;
  private TeamMemberRepository teamMemberRepository;
  private TeamMemberCommandServiceImpl teamMemberCommandServiceImpl;

  @BeforeEach
  void setUp() {
    teamRepository = mock(TeamRepository.class);
    userRepository = mock(UserRepository.class);
    teamMemberRepository = mock(TeamMemberRepository.class);
    teamMemberCommandServiceImpl =
        new TeamMemberCommandServiceImpl(teamRepository, userRepository, teamMemberRepository);
  }

  @Test
  @DisplayName("팀장 권한 양도 성공 테스트")
  void transferLeadership_success() {
    Long teamId = 1L;
    Long leaderId = 10L;
    Long targetId = 20L;
    String email = "newleader@example.com";

    TransferLeaderRequest request = new TransferLeaderRequest();
    ReflectionTestUtils.setField(request, "email", email);

    Team team = Team.builder().teamId(teamId).userId(leaderId).build();
    User target = createUser(targetId, email);
    TeamMember member = TeamMember.builder().team(team).user(target).build();
    setPrivateField(member, "id", new TeamMemberId(targetId, teamId));

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(target));
    when(teamMemberRepository.findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(targetId, teamId))
        .thenReturn(Optional.of(member));

    teamMemberCommandServiceImpl.transferLeadership(leaderId, teamId, request);

    assertThat(team.getUserId()).isEqualTo(targetId);
  }

  @Test
  @DisplayName("팀이 존재하지 않으면 예외 발생")
  void transferLeadership_shouldThrowException_whenTeamNotFound() {
    when(teamRepository.findById(1L)).thenReturn(Optional.empty());

    TransferLeaderRequest request = new TransferLeaderRequest();
    ReflectionTestUtils.setField(request, "email", "someone@a.com");

    assertThatThrownBy(() -> teamMemberCommandServiceImpl.transferLeadership(1L, 1L, request))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.TEAM_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("팀장이 아닌 사용자가 양도 시도 시 예외 발생")
  void transferLeadership_shouldThrowException_whenNotLeader() {
    Team team = Team.builder().teamId(1L).userId(999L).build();
    when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

    TransferLeaderRequest request = new TransferLeaderRequest();
    ReflectionTestUtils.setField(request, "email", "other@a.com");

    assertThatThrownBy(() -> teamMemberCommandServiceImpl.transferLeadership(1L, 1L, request))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.NOT_TEAM_LEADER.getMessage());
  }

  @Test
  @DisplayName("존재하지 않는 유저에게 양도 시 예외 발생")
  void transferLeadership_shouldThrowException_whenUserNotFound() {
    Team team = Team.builder().teamId(1L).userId(1L).build();
    when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
    when(userRepository.findUserByEmail("nouser@a.com")).thenReturn(Optional.empty());

    TransferLeaderRequest request = new TransferLeaderRequest();
    ReflectionTestUtils.setField(request, "email", "nouser@a.com");

    assertThatThrownBy(() -> teamMemberCommandServiceImpl.transferLeadership(1L, 1L, request))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.USER_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("팀원이 아닌 유저에게 양도 시 예외 발생")
  void transferLeadership_shouldThrowException_whenUserIsNotMember() {
    Team team = Team.builder().teamId(1L).userId(1L).build();
    User target = createUser(2L, "other@a.com");

    when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
    when(userRepository.findUserByEmail("other@a.com")).thenReturn(Optional.of(target));
    when(teamMemberRepository.findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(2L, 1L))
        .thenReturn(Optional.empty());

    TransferLeaderRequest request = new TransferLeaderRequest();
    ReflectionTestUtils.setField(request, "email", "other@a.com");

    assertThatThrownBy(() -> teamMemberCommandServiceImpl.transferLeadership(1L, 1L, request))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.NOT_TEAM_MEMBER.getMessage());
  }

  @Test
  @DisplayName("자기 자신에게 양도 시 예외 발생")
  void transferLeadership_shouldThrowException_whenTransferToSelf() {
    Long leaderId = 1L;
    String email = "self@a.com";

    Team team = Team.builder().teamId(1L).userId(leaderId).build();
    User leader = createUser(leaderId, email);
    TeamMember member = TeamMember.builder().team(team).user(leader).build();
    setPrivateField(member, "id", new TeamMemberId(leaderId, 1L));

    when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
    when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(leader));
    when(teamMemberRepository.findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(leaderId, 1L))
        .thenReturn(Optional.of(member));

    TransferLeaderRequest request = new TransferLeaderRequest();
    ReflectionTestUtils.setField(request, "email", email);

    assertThatThrownBy(() -> teamMemberCommandServiceImpl.transferLeadership(leaderId, 1L, request))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.CANNOT_TRANSFER_TO_SELF.getMessage());
  }

  // === 유틸 ===

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
