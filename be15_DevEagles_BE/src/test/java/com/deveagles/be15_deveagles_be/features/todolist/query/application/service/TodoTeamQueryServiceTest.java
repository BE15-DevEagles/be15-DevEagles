package com.deveagles.be15_deveagles_be.features.todolist.query.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.deveagles.be15_deveagles_be.common.dto.PagedResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.TeamFilteredTodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.mapper.TodoTeamQueryMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodoTeamQueryServiceTest {

  @Mock private TodoTeamQueryMapper todoTeamQueryMapper;

  @InjectMocks private TodoTeamQueryService todoTeamQueryService;

  @Test
  @DisplayName("특정 팀 todo 리스트를 필터링 조건으로 조회 성공")
  void getTeamTodosByCondition_success() {
    // given
    Long teamId = 1L;
    Long userId = 2L;
    String status = "incomplete";
    int page = 1;
    int size = 5;
    int offset = 0;

    List<TeamFilteredTodoResponse> mockList =
        List.of(
            TeamFilteredTodoResponse.builder()
                .todoId(100L)
                .content("코드 리뷰")
                .startDate(LocalDate.of(2025, 5, 20))
                .dueDate(LocalDate.of(2025, 5, 22))
                .userId(2L)
                .userName("니코")
                .userThumbnailUrl("https://example.com/profile.jpg")
                .build());

    // when
    when(todoTeamQueryMapper.selectTeamTodosByCondition(teamId, userId, status, offset, size))
        .thenReturn(mockList);
    when(todoTeamQueryMapper.countTeamTodosByCondition(teamId, userId, status)).thenReturn(1);

    // then
    PagedResponse<TeamFilteredTodoResponse> result =
        todoTeamQueryService.getTeamTodosByCondition(teamId, userId, status, page, size);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTodoId()).isEqualTo(100L);
    assertThat(result.getContent().get(0).getUserName()).isEqualTo("니코");
    assertThat(result.getPagination().getTotalPages()).isEqualTo(1);
    assertThat(result.getPagination().getCurrentPage()).isEqualTo(1);
  }
}
