package com.deveagles.be15_deveagles_be.features.team.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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

class FireTeamMemberTest {

  private TeamRepository teamRepository;
  private UserRepository userRepository;
  private TeamMemberRepository teamMemberRepository;
  private TeamMemberCommandServiceImpl teamCommandService;

  @BeforeEach
  void setUp() {
    teamRepository = mock(TeamRepository.class);
    userRepository = mock(UserRepository.class);
    teamMemberRepository = mock(TeamMemberRepository.class);
    teamCommandService =
        new TeamMemberCommandServiceImpl(teamRepository, userRepository, teamMemberRepository);
  }

  @Test
  @DisplayName("팀원 추방 - 성공")
  void fireTeamMember_success() {
    Long teamId = 1L;
    Long leaderId = 10L;
    Long memberId = 20L;
    String email = "kick@example.com";

    Team team = Team.builder().teamId(teamId).userId(leaderId).build();
    User member = createUser(memberId, email);
    TeamMember teamMember = TeamMember.builder().team(team).user(member).build();
    setPrivateField(teamMember, "id", new TeamMemberId(memberId, teamId));

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(member));
    when(teamMemberRepository.findByTeamTeamIdAndUserUserId(teamId, memberId))
        .thenReturn(Optional.of(teamMember));

    teamCommandService.fireTeamMember(leaderId, teamId, email);

    assertThat(teamMember.getDeletedAt()).isNotNull();
  }

  @Test
  @DisplayName("팀원 추방 - 존재하지 않는 팀일 경우 예외 발생")
  void fireTeamMember_teamNotFound() {
    Long teamId = 1L;
    when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> teamCommandService.fireTeamMember(1L, teamId, "email@example.com"))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.TEAM_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("팀원 추방 - 팀장이 아닌 사용자가 시도하면 예외 발생")
  void fireTeamMember_notTeamLeader() {
    Long teamId = 1L;
    Long leaderId = 999L;
    Team team = Team.builder().teamId(teamId).userId(1L).build();

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

    assertThatThrownBy(
            () -> teamCommandService.fireTeamMember(leaderId, teamId, "email@example.com"))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.NOT_TEAM_LEADER.getMessage());
  }

  @Test
  @DisplayName("팀원 추방 - 유저가 존재하지 않으면 예외 발생")
  void fireTeamMember_userNotFound() {
    Long teamId = 1L;
    Long leaderId = 1L;
    String email = "unknown@example.com";

    Team team = Team.builder().teamId(teamId).userId(leaderId).build();

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> teamCommandService.fireTeamMember(leaderId, teamId, email))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.USER_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("팀원 추방 - 팀장이 자신을 추방하려 할 경우 예외 발생")
  void fireTeamMember_cannotFireSelf() {
    Long teamId = 1L;
    Long leaderId = 1L;
    String email = "leader@example.com";

    Team team = Team.builder().teamId(teamId).userId(leaderId).build();
    User leader = createUser(leaderId, email);

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(leader));

    assertThatThrownBy(() -> teamCommandService.fireTeamMember(leaderId, teamId, email))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.CANNOT_FIRE_SELF.getMessage());
  }

  @Test
  @DisplayName("팀원 추방 - 대상이 팀원이 아닐 경우 예외 발생")
  void fireTeamMember_notTeamMember() {
    Long teamId = 1L;
    Long leaderId = 1L;
    Long targetId = 20L;
    String email = "notmember@example.com";

    Team team = Team.builder().teamId(teamId).userId(leaderId).build();
    User target = createUser(targetId, email);

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(target));
    when(teamMemberRepository.findByTeamTeamIdAndUserUserId(teamId, targetId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> teamCommandService.fireTeamMember(leaderId, teamId, email))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.NOT_TEAM_MEMBER.getMessage());
  }

  @Test
  @DisplayName("팀원 추방 - 이미 삭제된 팀원일 경우 예외 발생")
  void fireTeamMember_alreadyDeletedMember() {
    Long teamId = 1L;
    Long leaderId = 1L;
    Long targetId = 20L;
    String email = "deleted@example.com";

    Team team = Team.builder().teamId(teamId).userId(leaderId).build();
    User target = createUser(targetId, email);
    TeamMember teamMember = TeamMember.builder().team(team).user(target).build();
    setPrivateField(teamMember, "id", new TeamMemberId(targetId, teamId));
    teamMember.softDelete();

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(target));
    when(teamMemberRepository.findByTeamTeamIdAndUserUserId(teamId, targetId))
        .thenReturn(Optional.of(teamMember));

    assertThatThrownBy(() -> teamCommandService.fireTeamMember(leaderId, teamId, email))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.ALREADY_DELETED_MEMBER.getMessage());
  }

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
