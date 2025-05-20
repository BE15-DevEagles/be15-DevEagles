package com.deveagles.be15_deveagles_be.features.worklog.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request.WorkSummaryRequest;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.GeminiApiResponseDto;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.SummaryResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.service.GeneratorBuilder;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.service.WorklogService;
import com.deveagles.be15_deveagles_be.features.worklog.command.domain.exception.WorklogBusinessException;
import com.deveagles.be15_deveagles_be.features.worklog.command.domain.exception.WorklogErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WorklogServiceImpl implements WorklogService {
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private static final double DEFAULT_TEMPERATURE = 0.7;
  private static final int MAX_OUTPUT_TOKENS = 80;

  @Value("${gemini.api.summary}")
  private String summaryKey;

  @Value("${gemini.api.url}")
  private String apiUrl;

  public WorklogServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public SummaryResponse summaryGenerate(WorkSummaryRequest request) {
    String prompt =
        GeneratorBuilder.ContentsPrompt(request.getWorkContent(), request.getWorkContent());
    Map<String, Object> body =
        Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
            "generationConfig",
                Map.of(
                    "temperature", DEFAULT_TEMPERATURE,
                    "maxOutputTokens", MAX_OUTPUT_TOKENS));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> requestApi = new HttpEntity<>(body, headers);

    ResponseEntity<Map> response =
        restTemplate.exchange(apiUrl + summaryKey, HttpMethod.POST, requestApi, Map.class);

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
}
