package com.deveagles.be15_deveagles_be.features.team.command.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.team.command.application.service.impl.TeamMemberCommandServiceImpl;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.Team;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMember;
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
import org.junit.jupiter.api.Test;

class TeamMemberCommandServiceImplTest {

  private TeamRepository teamRepository;
  private UserRepository userRepository;
  private TeamMemberRepository teamMemberRepository;
  private TeamMemberCommandServiceImpl TeamMemberCommandServiceImpl;

  @BeforeEach
  void setUp() {
    teamRepository = mock(TeamRepository.class);
    userRepository = mock(UserRepository.class);
    teamMemberRepository = mock(TeamMemberRepository.class);
    TeamMemberCommandServiceImpl =
        new TeamMemberCommandServiceImpl(teamRepository, userRepository, teamMemberRepository);
  }

  @Test
  void 초대_성공() {
    // given
    Long teamId = 1L;
    Long inviterId = 10L;
    String email = "invitee@example.com";

    Team team = Team.builder().teamId(teamId).userId(inviterId).build();

    User invitee =
        User.builder()
            .email(email)
            .password("password")
            .userName("홍길동")
            .phoneNumber("01012345678")
            .userStatus(UserStatus.ENABLED)
            .createdAt(LocalDateTime.now())
            .modifiedAt(LocalDateTime.now())
            .deletedAt(null)
            .build();
    setPrivateField(invitee, "userId", 20L); // ⚠️ userId 수동 주입

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(invitee));
    when(teamMemberRepository.existsByTeamTeamIdAndUserUserId(teamId, 20L)).thenReturn(false);

    // when
    TeamMemberCommandServiceImpl.inviteTeamMember(inviterId, teamId, email);

    // then
    verify(teamMemberRepository, times(1)).save(any(TeamMember.class));
  }

  @Test
  void 팀이_없으면_예외() {
    Long teamId = 1L;
    when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> TeamMemberCommandServiceImpl.inviteTeamMember(1L, teamId, "email@example.com"))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.TEAM_NOT_FOUND.getMessage());
  }

  @Test
  void 팀장이_아니면_예외() {
    Long teamId = 1L;
    Long inviterId = 999L; // 팀장 아님
    Team team = Team.builder().teamId(teamId).userId(1L).build();

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

    assertThatThrownBy(
            () ->
                TeamMemberCommandServiceImpl.inviteTeamMember(
                    inviterId, teamId, "email@example.com"))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.NOT_TEAM_LEADER.getMessage());
  }

  @Test
  void 유저가_없으면_예외() {
    Long teamId = 1L;
    Long inviterId = 1L;
    String email = "notfound@example.com";
    Team team = Team.builder().teamId(teamId).userId(inviterId).build();

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> TeamMemberCommandServiceImpl.inviteTeamMember(inviterId, teamId, email))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.USER_NOT_FOUND.getMessage());
  }

  @Test
  void 이미_팀원이라면_예외() {
    Long teamId = 1L;
    Long inviterId = 1L;
    String email = "exists@example.com";

    Team team = Team.builder().teamId(teamId).userId(inviterId).build();

    User invitee =
        User.builder()
            .email(email)
            .password("password")
            .userName("기존팀원")
            .phoneNumber("01098765432")
            .userStatus(UserStatus.ENABLED)
            .createdAt(LocalDateTime.now())
            .modifiedAt(LocalDateTime.now())
            .build();
    setPrivateField(invitee, "userId", 2L); // ⚠️ userId 수동 설정

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(invitee));
    when(teamMemberRepository.existsByTeamTeamIdAndUserUserId(teamId, 2L)).thenReturn(true);

    assertThatThrownBy(
            () -> TeamMemberCommandServiceImpl.inviteTeamMember(inviterId, teamId, email))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.ALREADY_TEAM_MEMBER.getMessage());
  }

  // 🧪 userId 리플렉션 유틸
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
