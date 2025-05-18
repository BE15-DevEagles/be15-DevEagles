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

import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.GeminiApiException;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.GeminiApiAdapter.GeminiTextResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class GeminiApiAdapterTest {

  @Mock private RestTemplate restTemplate;

  private GeminiApiAdapter geminiApiAdapter;

  @BeforeEach
  void setUp() {
    // 수동으로 어댑터 생성 및 Mock 주입
    geminiApiAdapter = new GeminiApiAdapter(restTemplate);

    // @Value 어노테이션으로 주입되는 필드 값 설정
    ReflectionTestUtils.setField(geminiApiAdapter, "apiKey", "test-api-key");
    ReflectionTestUtils.setField(geminiApiAdapter, "apiUrl", "https://test-api-url");
    ReflectionTestUtils.setField(geminiApiAdapter, "apiTimeoutMs", 5000);
  }

  @Test
  @DisplayName("정상적인 API 응답 처리 테스트")
  void generateText_withValidResponse_returnsText() {
    // Given
    String promptText = "Hello, AI!";

    // API 응답 모킹
    Map<String, Object> content = new HashMap<>();
    content.put("parts", List.of(Map.of("text", "Hello, human!")));

    Map<String, Object> candidate = new HashMap<>();
    candidate.put("content", content);
    candidate.put("finishReason", "STOP");
    candidate.put("index", 0);
    candidate.put("safetyRatings", List.of());

    Map<String, Object> response = new HashMap<>();
    response.put("candidates", List.of(candidate));
    response.put("promptFeedback", Map.of("safetyRatings", List.of()));

    ResponseEntity<Map> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(responseEntity);

    // When
    GeminiTextResponse result = geminiApiAdapter.generateText(promptText);

    // Then
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals("Hello, human!", result.getText());

    verify(restTemplate, times(1))
        .exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
  }

  @Test
  @DisplayName("API 호출 예외 처리 테스트")
  void generateText_whenApiCallFails_throwsUnexpectedException() {
    // Given
    String promptText = "Hello, AI!";

    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
        .thenThrow(new RuntimeException("API 호출 실패"));

    // When & Then
    GeminiApiException.UnexpectedException exception =
        assertThrows(
            GeminiApiException.UnexpectedException.class,
            () -> geminiApiAdapter.generateText(promptText));

    assertTrue(exception.getMessage().contains("API 호출 실패"));
    assertEquals("UNEXPECTED_ERROR", exception.getErrorCode());

    verify(restTemplate, times(1))
        .exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
  }

  @Test
  @DisplayName("불완전한 API 응답 처리 테스트")
  void generateText_withIncompleteResponse_throwsParsingException() {
    // Given
    String promptText = "Hello, AI!";

    // 불완전한 응답 모킹 (candidates 없음)
    Map<String, Object> response = new HashMap<>();
    response.put("promptFeedback", Map.of("safetyRatings", List.of()));

    ResponseEntity<Map> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(responseEntity);

    // When & Then
    GeminiApiException.ParsingException exception =
        assertThrows(
            GeminiApiException.ParsingException.class,
            () -> geminiApiAdapter.generateText(promptText));

    assertTrue(exception.getMessage().contains("텍스트를 추출할 수 없습니다"));
    assertEquals("TEXT_EXTRACTION_FAILED", exception.getErrorCode());

    verify(restTemplate, times(1))
        .exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
  }
}
