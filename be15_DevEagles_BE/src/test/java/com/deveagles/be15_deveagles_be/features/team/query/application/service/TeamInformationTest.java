package com.deveagles.be15_deveagles_be.features.team.query.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMember;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.TeamInformationResponse;
import com.deveagles.be15_deveagles_be.features.team.query.mapper.TeamMapper;
import com.deveagles.be15_deveagles_be.features.team.query.service.impl.TeamQueryServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TeamInformationTest {

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
  @DisplayName("팀 정보 조회 - 성공")
  void getTeamInformation_success() {
    // given
    Long userId = 1L;
    Long teamId = 100L;

    TeamMember mockMember = mock(TeamMember.class);
    when(teamMemberRepository.findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(userId, teamId))
        .thenReturn(Optional.of(mockMember));

    TeamInformationResponse expected =
        TeamInformationResponse.builder()
            .teamId(teamId)
            .teamName("Dev Eagles")
            .introduction("열정 가득한 개발팀입니다.")
            .url("https://example.com/team.jpg")
            .build();

    when(teamMapper.selectTeamInformation(teamId)).thenReturn(expected);

    // when
    TeamInformationResponse result = teamQueryService.getTeamInformation(userId, teamId);

    // then
    assertThat(result.getTeamId()).isEqualTo(expected.getTeamId());
    assertThat(result.getTeamName()).isEqualTo(expected.getTeamName());
    assertThat(result.getIntroduction()).isEqualTo(expected.getIntroduction());
    assertThat(result.getUrl()).isEqualTo(expected.getUrl());

    verify(teamMemberRepository).findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(userId, teamId);
    verify(teamMapper).selectTeamInformation(teamId);
  }

  @Test
  @DisplayName("팀 정보 조회 - 팀원이 아닌 경우 예외 발생")
  void getTeamInformation_shouldThrowException_ifNotTeamMember() {
    // given
    Long userId = 99L;
    Long teamId = 100L;

    when(teamMemberRepository.findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(userId, teamId))
        .thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> teamQueryService.getTeamInformation(userId, teamId))
        .isInstanceOf(TeamBusinessException.class)
        .hasMessageContaining(TeamErrorCode.NOT_TEAM_MEMBER.getMessage());

    verify(teamMemberRepository).findByIdUserIdAndIdTeamIdAndDeletedAtIsNull(userId, teamId);
    verify(teamMapper, never()).selectTeamInformation(anyLong());
  }
}
