package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.HuggingFaceApiException;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.HuggingFaceApiAdapter.EmotionAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class HuggingFaceApiAdapterTest {

  @Mock private RestTemplate restTemplate;

  @Spy
  @Qualifier("restTemplateObjectMapper") private ObjectMapper objectMapper = new ObjectMapper();

  private HuggingFaceApiAdapter huggingFaceApiAdapter;

  @BeforeEach
  void setUp() {
    // 수동으로 어댑터 생성 및 Mock 주입
    huggingFaceApiAdapter = new HuggingFaceApiAdapter(restTemplate, objectMapper);

    // @Value 어노테이션으로 주입되는 필드 값 설정
    ReflectionTestUtils.setField(huggingFaceApiAdapter, "apiKey", "test-api-key");
    ReflectionTestUtils.setField(huggingFaceApiAdapter, "apiUrl", "https://test-api-url");
  }

  @Test
  @DisplayName("정상적인 감정 분석 응답 처리 테스트")
  void analyzeEmotion_withValidResponse_returnsEmotionAnalysis() {
    // Given
    String text = "오늘은 정말 기분이 좋아요!";

    // HuggingFace API의 응답을 구조화된 형태로 모킹
    List<List<Map<String, Object>>> apiResponse = new ArrayList<>();
    List<Map<String, Object>> emotionsList = new ArrayList<>();

    emotionsList.add(Map.of("label", "JOY", "score", 0.8));
    emotionsList.add(Map.of("label", "SADNESS", "score", 0.1));
    emotionsList.add(Map.of("label", "ANGER", "score", 0.05));
    emotionsList.add(Map.of("label", "FEAR", "score", 0.03));
    emotionsList.add(Map.of("label", "SURPRISE", "score", 0.02));

    apiResponse.add(emotionsList);

    ResponseEntity<Object> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
        .thenReturn(responseEntity);

    // When
    EmotionAnalysisResponse result = huggingFaceApiAdapter.analyzeEmotion(text);

    // Then
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals("JOY", result.getDominantEmotion());
    assertEquals(0.8, result.getDominantScore());
    assertEquals(5, result.getEmotions().size());

    verify(restTemplate, times(1))
        .postForEntity(anyString(), any(HttpEntity.class), eq(Object.class));
  }

  @Test
  @DisplayName("API 호출 예외 처리 테스트")
  void analyzeEmotion_whenApiCallFails_throwsUnexpectedException() {
    // Given
    String text = "오늘은 정말 기분이 좋아요!";

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
        .thenThrow(new RuntimeException("API 호출 실패"));

    // When & Then
    HuggingFaceApiException.UnexpectedException exception =
        assertThrows(
            HuggingFaceApiException.UnexpectedException.class,
            () -> huggingFaceApiAdapter.analyzeEmotion(text));

    assertTrue(exception.getMessage().contains("API 호출 실패"));
    assertEquals("UNEXPECTED_ERROR", exception.getErrorCode());

    verify(restTemplate, times(1))
        .postForEntity(anyString(), any(HttpEntity.class), eq(Object.class));
  }

  @Test
  @DisplayName("예상치 못한 응답 형식 처리 테스트")
  void analyzeEmotion_withUnexpectedResponseFormat_throwsParsingException() {
    // Given
    String text = "오늘은 정말 기분이 좋아요!";

    // 예상치 못한 형식의 응답 (List<List<Map>> 구조가 아님)
    Map<String, String> unexpectedResponse = Map.of("error", "Unexpected format");

    ResponseEntity<Object> responseEntity = new ResponseEntity<>(unexpectedResponse, HttpStatus.OK);

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
        .thenReturn(responseEntity);

    // When & Then
    HuggingFaceApiException.ParsingException exception =
        assertThrows(
            HuggingFaceApiException.ParsingException.class,
            () -> huggingFaceApiAdapter.analyzeEmotion(text));

    assertTrue(exception.getMessage().contains("감정 분석 결과를 추출할 수 없습니다"));
    assertEquals("EMOTION_EXTRACTION_FAILED", exception.getErrorCode());

    verify(restTemplate, times(1))
        .postForEntity(anyString(), any(HttpEntity.class), eq(Object.class));
  }

  @Test
  @DisplayName("빈 응답 처리 테스트")
  void analyzeEmotion_withEmptyResponse_throwsParsingException() {
    // Given
    String text = "오늘은 정말 기분이 좋아요!";

    // 빈 응답
    List<List<Map<String, Object>>> emptyResponse = new ArrayList<>();

    ResponseEntity<Object> responseEntity = new ResponseEntity<>(emptyResponse, HttpStatus.OK);

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
        .thenReturn(responseEntity);

    // When & Then
    HuggingFaceApiException.ParsingException exception =
        assertThrows(
            HuggingFaceApiException.ParsingException.class,
            () -> huggingFaceApiAdapter.analyzeEmotion(text));

    assertTrue(exception.getMessage().contains("감정 분석 결과를 추출할 수 없습니다"));
    assertEquals("EMOTION_EXTRACTION_FAILED", exception.getErrorCode());

    verify(restTemplate, times(1))
        .postForEntity(anyString(), any(HttpEntity.class), eq(Object.class));
  }
}
