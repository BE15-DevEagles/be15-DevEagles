package com.deveagles.be15_deveagles_be.features.team.query.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMember;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.TeamMemberResponse;
import com.deveagles.be15_deveagles_be.features.team.query.mapper.TeamMapper;
import com.deveagles.be15_deveagles_be.features.team.query.service.impl.TeamQueryServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TeamMemberListTest {

  private TeamMapper teamMapper;
  private TeamMemberRepository teamMemberRepository;
  private TeamQueryServiceImpl teamQueryService;

  @BeforeEach
  void setUp() {
    teamMapper = mock(TeamMapper.class);
    teamMemberRepository = mock(TeamMemberRepository.class);
    teamQueryService = new TeamQueryServiceImpl(teamMapper, teamMemberRepository);
  }

  @Test
  @DisplayName("팀원 목록 조회 - 성공")
  void getTeamMembers_success() {
    // given
    Long userId = 1L;
    Long teamId = 10L;

    TeamMember mockMember = mock(TeamMember.class);
    when(teamMemberRepository.findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(userId, teamId))
        .thenReturn(Optional.of(mockMember));

    TeamMemberResponse member1 =
        TeamMemberResponse.builder().userId(1L).email("hong@example.com").build();

    when(teamMapper.selectTeamMembers(teamId)).thenReturn(List.of(member1));

    // when
    List<TeamMemberResponse> result = teamQueryService.getTeamMembers(userId, teamId);

    // then
    assertThat(result).hasSize(1);
    verify(teamMapper).selectTeamMembers(teamId);
  }

  @Test
  @DisplayName("팀원이 아닌 경우 예외 발생")
  void getTeamMembers_shouldThrowException_ifNotTeamMember() {
    // given
    Long userId = 99L;
    Long teamId = 10L;

    when(teamMemberRepository.findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(userId, teamId))
        .thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> teamQueryService.getTeamMembers(userId, teamId))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.NOT_TEAM_MEMBER.getMessage());
  }
}
