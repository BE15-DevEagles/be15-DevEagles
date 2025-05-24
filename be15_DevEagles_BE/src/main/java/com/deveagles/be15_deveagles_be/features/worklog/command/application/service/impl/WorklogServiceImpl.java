package com.deveagles.be15_deveagles_be.features.worklog.command.application.service.impl;

import com.deveagles.be15_deveagles_be.common.dto.PagedResponse;
import com.deveagles.be15_deveagles_be.common.dto.Pagination;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.TeamMemberResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.TeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamCommandService;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamMemberCommandService;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandService;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserErrorCode;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.SearchWorklogRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.WorkSummaryRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.WorklogCreateRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.GeminiApiResponseDto;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.SummaryResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogDetailResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.service.GeneratorBuilder;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.service.PageRequestUtil;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.service.WorklogService;
import com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate.Worklog;
import com.deveagles.be15_deveagles_be.features.worklog.command.domain.exception.WorklogBusinessException;
import com.deveagles.be15_deveagles_be.features.worklog.command.domain.exception.WorklogErrorCode;
import com.deveagles.be15_deveagles_be.features.worklog.command.domain.repository.WorklogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class WorklogServiceImpl implements WorklogService {
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private static final double DEFAULT_TEMPERATURE = 0.7;
  private static final int MAX_OUTPUT_TOKENS = 80;
  private final WorklogRepository worklogRepository;
  private final UserCommandService userCommandService;
  private final TeamCommandService teamCommandService;
  private final TeamMemberCommandService teamMemberCommandService;

  @Value("${gemini.api.summary}")
  private String summaryKey;

  @Value("${gemini.api.url}")
  private String apiUrl;

  public WorklogServiceImpl(
      RestTemplate restTemplate,
      ObjectMapper objectMapper,
      WorklogRepository worklogRepository,
      UserCommandService userCommandService,
      TeamCommandService teamCommandService,
      TeamMemberCommandService teamMemberCommandService) {
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
    this.worklogRepository = worklogRepository;
    this.userCommandService = userCommandService;
    this.teamCommandService = teamCommandService;
    this.teamMemberCommandService = teamMemberCommandService;
  }

  @Transactional
  @Override
  public SummaryResponse summaryGenerate(Long userId, WorkSummaryRequest request) {
    validateUserExists(userId);
    String prompt =
        GeneratorBuilder.ContentsPrompt(request.getWorkContent(), request.getWorkContent());
    Map<String, Object> body =
        Map.of(
            "contents",
            List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
            "generationConfig",
            Map.of(
                "temperature", DEFAULT_TEMPERATURE,
                "maxOutputTokens", MAX_OUTPUT_TOKENS));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> requestApi = new HttpEntity<>(body, headers);

    ResponseEntity<Map> response =
        restTemplate.exchange(
            apiUrl + "?key=" + summaryKey, HttpMethod.POST, requestApi, Map.class);

    Map<?, ?> responseBody = response.getBody();

    if (responseBody == null) {
      throw new WorklogBusinessException(
          WorklogErrorCode.GEMINI_API_ERROR, "GEMINI API 응답이 비어있습니다.");
    }

    try {
      GeminiApiResponseDto geminiResponse =
          objectMapper.convertValue(responseBody, GeminiApiResponseDto.class);

      // 후보 리스트 및 내용 검증
      if (geminiResponse.getCandidates() == null || geminiResponse.getCandidates().isEmpty()) {
        throw new WorklogBusinessException(
            WorklogErrorCode.GEMINI_API_ERROR, "Gemini API 응답에 후보 텍스트가 없습니다.");
      }

      GeminiApiResponseDto.Candidate firstCandidate = geminiResponse.getCandidates().get(0);

      if (firstCandidate.getContent() == null) {
        throw new WorklogBusinessException(
            WorklogErrorCode.GEMINI_API_ERROR, "Gemini API 응답에 content 필드가 없습니다.");
      }

      List<GeminiApiResponseDto.Part> parts = firstCandidate.getContent().getParts();

      if (parts == null || parts.isEmpty()) {
        throw new WorklogBusinessException(
            WorklogErrorCode.GEMINI_API_ERROR, "Gemini API 응답에 parts 데이터가 없습니다.");
      }

      String summary = parts.get(0).getText();

      if (summary == null || summary.trim().isEmpty()) {
        throw new WorklogBusinessException(
            WorklogErrorCode.GEMINI_API_ERROR, "Gemini API 요약 결과가 비어있습니다.");
      }

      return SummaryResponse.builder().summary(summary.trim()).build();

    } catch (ClassCastException | NullPointerException e) {
      throw new WorklogBusinessException(
          WorklogErrorCode.GEMINI_API_ERROR, "Gemini API 응답 구조가 올바르지 않습니다");
    }
  }

  /* 업무일지 등록하기 */
  @Transactional
  @Override
  public WorklogDetailResponse createWorklog(
      Long userId, Long teamId, WorklogCreateRequest worklogCreateRequest) {
    if (worklogRepository.existsByTeamIdAndWrittenAt(teamId, worklogCreateRequest.getWrittenAt())) {
      throw new WorklogBusinessException(WorklogErrorCode.WORKLOG_ALREADY_EXISTS);
    }
    validateUserExists(userId);
    validateTeamExists(teamId);
    validateTeamMemberExists(teamId, userId);

    return WorklogDetailResponse.of(
        worklogRepository.save(
            Worklog.builder()
                .summary(worklogCreateRequest.getSummary())
                .workContent(worklogCreateRequest.getWorkContent())
                .note(worklogCreateRequest.getNote())
                .planContent(worklogCreateRequest.getPlanContent())
                .teamId(teamId)
                .userId(userId)
                .writtenAt(worklogCreateRequest.getWrittenAt())
                .build()));
  }

  @Transactional
  @Override
  public PagedResponse<WorklogResponse> findMyWorklog(Long userId, SearchWorklogRequest request) {
    Long teamId = request.getTeamId();
    validateUserExists(userId);
    validateTeamExists(teamId);
    validateTeamMemberExists(teamId, userId);

    Pageable pageable = PageRequestUtil.createPageRequest(request.getPage(), request.getSize());

    Page<Worklog> myWorklogPage = worklogRepository.findByUserIdAndTeamId(userId, teamId, pageable);

    String userName = userCommandService.getUserDetails(userId).getUserName();
    String teamName = teamCommandService.getTeamDetail(teamId).getTeamName();
    List<WorklogResponse> worklogResponses =
        myWorklogPage.getContent().stream()
            .map(
                w ->
                    WorklogResponse.builder()
                        .worklogId(w.getWorklogId())
                        .userName(userName)
                        .teamName(teamName)
                        .summary(w.getSummary())
                        .writtenAt(w.getWrittenAt())
                        .build())
            .collect(Collectors.toList());

    Pagination pagination =
        Pagination.builder()
            .currentPage(myWorklogPage.getNumber() + 1)
            .totalPages(myWorklogPage.getTotalPages())
            .totalItems(myWorklogPage.getTotalElements())
            .build();

    return new PagedResponse<>(worklogResponses, pagination);
  }

  @Transactional(readOnly = true)
  @Override
  public PagedResponse<WorklogResponse> findTeamWorklogs(
      Long userId, SearchWorklogRequest request) {
    validateTeamExists(request.getTeamId());
    validateTeamMemberExists(request.getTeamId(), userId);
    String teamName = teamCommandService.getTeamDetail(request.getTeamId()).getTeamName();
    Pageable pageable = PageRequestUtil.createPageRequest(request.getPage(), request.getSize());
    Page<Worklog> worklogPage = worklogRepository.findByTeamId(request.getTeamId(), pageable);
    List<WorklogResponse> contents =
        worklogPage.getContent().stream()
            .map(
                w ->
                    WorklogResponse.builder()
                        .worklogId(w.getWorklogId())
                        .userName(userCommandService.getUserDetails(w.getUserId()).getUserName())
                        .teamName(teamName)
                        .summary(w.getSummary())
                        .writtenAt(w.getWrittenAt())
                        .build())
            .toList();

    Pagination pagination =
        new Pagination(
            worklogPage.getNumber() + 1,
            worklogPage.getTotalPages(),
            worklogPage.getTotalElements());

    return new PagedResponse<>(contents, pagination);
  }

  @Transactional(readOnly = true)
  @Override
  public WorklogDetailResponse getWorklogById(Long worklogId, Long userId) {
    Worklog worklog =
        worklogRepository
            .findById(worklogId)
            .orElseThrow(
                () ->
                    new WorklogBusinessException(
                        WorklogErrorCode.INVALID_WORKLOG_INPUT, "업무일지를 찾을 수 없습니다."));

    Long teamId = worklog.getTeamId();
    validateTeamMemberExists(teamId, userId);
    String userName = userCommandService.getUserDetails(userId).getUserName();
    String teamName = teamCommandService.getTeamDetail(teamId).getTeamName();

    return WorklogDetailResponse.builder()
        .worklogId(worklog.getWorklogId())
        .summary(worklog.getSummary())
        .workContent(worklog.getWorkContent())
        .note(worklog.getNote())
        .planContent(worklog.getPlanContent())
        .userName(userName)
        .teamName(teamName)
        .teamId(teamId)
        .userId(userId)
        .writtenAt(worklog.getWrittenAt())
        .build();
  }

  public void validateUserExists(Long userId) {
    UserDetailResponse detail = userCommandService.getUserDetails(userId);
    if (detail == null || detail.getUserId() == null) {
      throw new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION);
    }
  }

  public void validateTeamExists(Long teamId) {
    TeamResponse detail = teamCommandService.getTeamDetail(teamId);
    if (detail == null || detail.getTeamId() == null) {
      throw new TeamBusinessException(TeamErrorCode.TEAM_NOT_FOUND);
    }
  }

  public void validateTeamMemberExists(Long teamId, Long userId) {
    TeamMemberResponse detail = teamMemberCommandService.findTeamMember(userId, teamId);
    if (detail == null || detail.getTeamId() == null || detail.getUserId() == null) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_MEMBER);
    }
  }
}
