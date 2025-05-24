package com.deveagles.be15_deveagles_be.features.worklog.command.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.common.dto.PagedResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.TeamMemberResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.TeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamCommandService;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamMemberCommandService;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandService;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.SearchWorklogRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.WorklogCreateRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogDetailResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate.Worklog;
import com.deveagles.be15_deveagles_be.features.worklog.command.domain.exception.WorklogBusinessException;
import com.deveagles.be15_deveagles_be.features.worklog.command.domain.repository.WorklogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WorklogServiceImplTest {

  @Mock private WorklogRepository worklogRepository;
  @Mock private UserCommandService userCommandService;
  @Mock private TeamCommandService teamCommandService;
  @Mock private TeamMemberCommandService teamMemberCommandService;

  @InjectMocks private WorklogServiceImpl worklogService;

  @Test
  @DisplayName("업무일지 등록 성공")
  void createWorklog_success() {
    Long userId = 1L;
    Long teamId = 10L;
    LocalDateTime writtenAt = LocalDateTime.of(2025, 5, 24, 0, 0);

    WorklogCreateRequest request =
        WorklogCreateRequest.builder()
            .summary("요약")
            .workContent("작업 내용")
            .note("특이사항")
            .planContent("계획")
            .writtenAt(writtenAt)
            .build();

    when(worklogRepository.existsByTeamIdAndWrittenAt(teamId, writtenAt)).thenReturn(false);
    when(userCommandService.getUserDetails(userId))
        .thenReturn(UserDetailResponse.builder().userId(userId).userName("홍길동").build());
    when(teamCommandService.getTeamDetail(teamId))
        .thenReturn(TeamResponse.builder().teamId(teamId).teamName("BE팀").build());
    when(teamMemberCommandService.findTeamMember(userId, teamId))
        .thenReturn(TeamMemberResponse.builder().userId(userId).teamId(teamId).build());

    Worklog saved =
        Worklog.builder()
            .summary("요약")
            .workContent("작업 내용")
            .note("특이사항")
            .planContent("계획")
            .userId(userId)
            .teamId(teamId)
            .writtenAt(writtenAt)
            .build();
    ReflectionTestUtils.setField(saved, "worklogId", 100L);

    when(worklogRepository.save(any(Worklog.class))).thenReturn(saved);

    WorklogDetailResponse response = worklogService.createWorklog(userId, teamId, request);

    assertEquals(100L, response.getWorklogId());
    assertEquals("요약", response.getSummary());
  }

  @Test
  @DisplayName("내 업무일지 조회 성공")
  void findMyWorklog_success() {
    Long userId = 1L;
    Long teamId = 100L;
    LocalDateTime writtenAt = LocalDateTime.of(2025, 5, 24, 0, 0);

    SearchWorklogRequest request =
        SearchWorklogRequest.builder().teamId(teamId).page(1).size(10).build();

    Worklog worklog =
        Worklog.builder()
            .userId(userId)
            .teamId(teamId)
            .summary("내 업무일지")
            .writtenAt(writtenAt)
            .build();
    ReflectionTestUtils.setField(worklog, "worklogId", 123L);

    Page<Worklog> page = new PageImpl<>(List.of(worklog), PageRequest.of(0, 10), 1);

    when(teamMemberCommandService.findTeamMember(userId, teamId))
        .thenReturn(TeamMemberResponse.builder().userId(userId).teamId(teamId).build());
    when(userCommandService.getUserDetails(userId))
        .thenReturn(UserDetailResponse.builder().userId(userId).userName("홍길동").build());
    when(teamCommandService.getTeamDetail(teamId))
        .thenReturn(TeamResponse.builder().teamId(teamId).teamName("BE팀").build());
    when(worklogRepository.findByUserIdAndTeamId(eq(userId), eq(teamId), any())).thenReturn(page);

    PagedResponse<WorklogResponse> result = worklogService.findMyWorklog(userId, request);

    assertEquals(1, result.getContent().size());
    assertEquals(123L, result.getContent().get(0).getWorklogId());
  }

  @Test
  @DisplayName("팀 업무일지 조회 성공")
  void findTeamWorklogs_success() {
    Long userId = 2L;
    Long teamId = 200L;

    SearchWorklogRequest request =
        SearchWorklogRequest.builder().teamId(teamId).page(1).size(10).build();

    Worklog worklog =
        Worklog.builder()
            .userId(3L)
            .teamId(teamId)
            .summary("팀 업무일지")
            .writtenAt(LocalDateTime.of(2025, 5, 23, 0, 0))
            .build();
    ReflectionTestUtils.setField(worklog, "worklogId", 456L);

    Page<Worklog> page = new PageImpl<>(List.of(worklog), PageRequest.of(0, 10), 1);

    when(teamMemberCommandService.findTeamMember(userId, teamId))
        .thenReturn(TeamMemberResponse.builder().userId(userId).teamId(teamId).build());
    when(userCommandService.getUserDetails(3L))
        .thenReturn(UserDetailResponse.builder().userId(3L).userName("이순신").build());
    when(teamCommandService.getTeamDetail(teamId))
        .thenReturn(TeamResponse.builder().teamId(teamId).teamName("FE팀").build());
    when(worklogRepository.findByTeamId(eq(teamId), any())).thenReturn(page);

    PagedResponse<WorklogResponse> result = worklogService.findTeamWorklogs(userId, request);

    assertEquals(1, result.getContent().size());
    assertEquals(456L, result.getContent().get(0).getWorklogId());
  }

  @Test
  @DisplayName("업무일지 상세조회 성공")
  void getWorklogById_success() {
    Long userId = 1L;
    Long teamId = 10L;
    Long worklogId = 789L;
    LocalDateTime writtenAt = LocalDateTime.of(2025, 5, 22, 0, 0);

    Worklog worklog =
        Worklog.builder()
            .userId(userId)
            .teamId(teamId)
            .summary("상세조회")
            .note("note")
            .planContent("plan")
            .writtenAt(writtenAt)
            .build();
    ReflectionTestUtils.setField(worklog, "worklogId", worklogId);

    when(worklogRepository.findById(worklogId)).thenReturn(Optional.of(worklog));
    when(teamMemberCommandService.findTeamMember(userId, teamId))
        .thenReturn(TeamMemberResponse.builder().userId(userId).teamId(teamId).build());
    when(userCommandService.getUserDetails(userId))
        .thenReturn(UserDetailResponse.builder().userId(userId).userName("홍길동").build());
    when(teamCommandService.getTeamDetail(teamId))
        .thenReturn(TeamResponse.builder().teamId(teamId).teamName("BE팀").build());

    WorklogDetailResponse response = worklogService.getWorklogById(worklogId, userId);

    assertEquals(worklogId, response.getWorklogId());
    assertEquals("상세조회", response.getSummary());
    assertEquals("홍길동", response.getUserName());
  }

  @Test
  @DisplayName("중복된 날짜에 업무일지 등록 시 예외 발생")
  void createWorklog_fail_whenDuplicateWrittenAtExists() {
    Long userId = 1L;
    Long teamId = 100L;
    LocalDateTime writtenAt = LocalDateTime.of(2025, 5, 24, 0, 0);

    WorklogCreateRequest request =
        WorklogCreateRequest.builder()
            .summary("중복 테스트")
            .workContent("중복 내용")
            .note("없음")
            .planContent("중복 계획")
            .writtenAt(writtenAt)
            .build();

    when(worklogRepository.existsByTeamIdAndWrittenAt(teamId, writtenAt)).thenReturn(true);

    assertThrows(
        WorklogBusinessException.class,
        () -> worklogService.createWorklog(userId, teamId, request));
  }

  @Test
  @DisplayName("존재하지 않는 사용자로 내 업무일지 조회 시 예외 발생")
  void findMyWorklog_fail_whenUserDoesNotExist() {
    Long userId = 999L;
    Long teamId = 100L;

    SearchWorklogRequest request =
        SearchWorklogRequest.builder().teamId(teamId).page(1).size(10).build();

    when(userCommandService.getUserDetails(userId)).thenReturn(null);

    assertThrows(UserBusinessException.class, () -> worklogService.findMyWorklog(userId, request));
  }

  @Test
  @DisplayName("팀 멤버가 아닌 사용자가 상세조회 시 예외 발생")
  void getWorklogById_fail_whenUserNotInTeam() {
    Long userId = 1L;
    Long teamId = 10L;
    Long worklogId = 555L;

    Worklog worklog =
        Worklog.builder()
            .userId(3L)
            .teamId(teamId)
            .summary("다른 사람의 업무일지")
            .writtenAt(LocalDateTime.of(2025, 5, 23, 0, 0))
            .build();
    ReflectionTestUtils.setField(worklog, "worklogId", worklogId);

    when(worklogRepository.findById(worklogId)).thenReturn(Optional.of(worklog));
    when(teamMemberCommandService.findTeamMember(userId, teamId)).thenReturn(null);

    assertThrows(
        TeamBusinessException.class, () -> worklogService.getWorklogById(worklogId, userId));
  }
}
