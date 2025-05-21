package com.deveagles.be15_deveagles_be.features.team.command.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.amazonaws.services.s3.AmazonS3;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.impl.TeamCommandServiceImpl;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.Team;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMember;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMemberId;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamRepository;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.UserStatus;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeleteTeamTest {

  private TeamRepository teamRepository;
  private TeamMemberRepository teamMemberRepository;
  private TeamCommandServiceImpl teamCommandService;

  @BeforeEach
  void setUp() {
    teamRepository = mock(TeamRepository.class);
    teamMemberRepository = mock(TeamMemberRepository.class);
    AmazonS3 amazonS3 = mock(AmazonS3.class);
    teamCommandService =
        new TeamCommandServiceImpl(teamRepository, null, teamMemberRepository, amazonS3);
  }

  @Test
  @DisplayName("팀 삭제 성공 - 팀장 1명만 존재할 경우")
  void deleteTeam_success() {
    Long teamId = 1L;
    Long leaderId = 10L;
    User leader = createUser(leaderId, "leader@example.com");

    Team team = Team.builder().teamId(teamId).userId(leaderId).build();

    TeamMember teamMember = TeamMember.builder().user(leader).team(team).build();
    setPrivateField(teamMember, "id", new TeamMemberId(leaderId, teamId));

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(teamMemberRepository.findByTeamTeamIdAndDeletedAtIsNull(teamId))
        .thenReturn(List.of(teamMember));

    teamCommandService.deleteTeam(leaderId, teamId);

    assertThat(team.getDeletedAt()).isNotNull();
    verify(teamRepository).findById(teamId);
    verify(teamMemberRepository).findByTeamTeamIdAndDeletedAtIsNull(teamId);
  }

  @Test
  @DisplayName("팀 삭제 실패 - 팀이 존재하지 않음")
  void deleteTeam_fail_teamNotFound() {
    when(teamRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> teamCommandService.deleteTeam(1L, 1L))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.TEAM_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("팀 삭제 실패 - 요청자가 팀장이 아님")
  void deleteTeam_fail_notLeader() {
    Long teamId = 1L;
    Long leaderId = 10L;
    Long attackerId = 20L;

    Team team = Team.builder().teamId(teamId).userId(leaderId).build();
    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

    assertThatThrownBy(() -> teamCommandService.deleteTeam(attackerId, teamId))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.NOT_TEAM_LEADER.getMessage());
  }

  @Test
  @DisplayName("팀 삭제 실패 - 팀장 외 팀원이 존재할 경우")
  void deleteTeam_fail_teamHasOtherMembers() {
    Long teamId = 1L;
    Long leaderId = 10L;
    Long memberId = 20L;

    Team team = Team.builder().teamId(teamId).userId(leaderId).build();
    User leader = createUser(leaderId, "leader@example.com");
    User member = createUser(memberId, "member@example.com");

    TeamMember leaderMember = TeamMember.builder().user(leader).team(team).build();
    setPrivateField(leaderMember, "id", new TeamMemberId(leaderId, teamId));

    TeamMember otherMember = TeamMember.builder().user(member).team(team).build();
    setPrivateField(otherMember, "id", new TeamMemberId(memberId, teamId));

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(teamMemberRepository.findByTeamTeamIdAndDeletedAtIsNull(teamId))
        .thenReturn(List.of(leaderMember, otherMember));

    assertThatThrownBy(() -> teamCommandService.deleteTeam(leaderId, teamId))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.TEAM_HAS_MEMBERS.getMessage());
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
