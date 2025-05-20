package com.deveagles.be15_deveagles_be.features.team.query.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import com.deveagles.be15_deveagles_be.features.team.query.dto.response.MyTeamListResponse;
import com.deveagles.be15_deveagles_be.features.team.query.mapper.TeamMapper;
import com.deveagles.be15_deveagles_be.features.team.query.service.impl.TeamQueryServiceImpl;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GetMyTeamListTest {

  private TeamMapper teamMapper;
  private TeamMemberRepository teamMemberRepository;
  private TeamQueryServiceImpl teamQueryService;

  @BeforeEach
  void setUp() {
    teamMapper = mock(TeamMapper.class);
    teamQueryService = new TeamQueryServiceImpl(teamMapper, teamMemberRepository);
  }

  @Test
  @DisplayName("사용자의 팀 목록 조회 성공")
  void getMyTeamList_success() {
    // given
    Long userId = 1L;
    List<MyTeamListResponse> expected =
        Arrays.asList(
            MyTeamListResponse.builder()
                .teamId(1L)
                .teamName("Team A")
                .teamThumbnailUrl("url1")
                .build(),
            MyTeamListResponse.builder()
                .teamId(2L)
                .teamName("Team B")
                .teamThumbnailUrl("url2")
                .build());

    when(teamMapper.selectMyTeamList(userId)).thenReturn(expected);

    // when
    List<MyTeamListResponse> result = teamQueryService.getTeamsByUserId(userId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getTeamId()).isEqualTo(1L);
    assertThat(result.get(0).getTeamName()).isEqualTo("Team A");

    verify(teamMapper).selectMyTeamList(userId);
  }
}
