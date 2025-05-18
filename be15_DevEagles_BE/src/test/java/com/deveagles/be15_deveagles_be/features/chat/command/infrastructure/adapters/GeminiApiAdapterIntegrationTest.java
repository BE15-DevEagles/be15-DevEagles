package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.GeminiApiAdapter.GeminiTextResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Gemini API 실제 연결 통합 테스트 */
// @Disabled
@SpringBootTest
public class GeminiApiAdapterIntegrationTest {

  @Autowired private GeminiApiAdapter geminiApiAdapter;

  @Test
  @DisplayName("질문에 올바른 답변 생성 확인")
  void whenGenerateText_thenReturnValidResponse() {
    // Given
    String prompt = "안녕하세요, 오늘 기분이 어떤지 물어봐주세요.";

    // When
    GeminiTextResponse response = geminiApiAdapter.generateText(prompt);

    // Then
    assertNotNull(response);
    assertFalse(response.isEmpty());
    assertFalse(response.getText().isEmpty());
    System.out.println("Gemini API 응답: " + response.getText());
  }

  @Test
  @DisplayName("복잡한 프롬프트로 답변 생성 확인")
  void whenGenerateTextWithComplexPrompt_thenReturnValidResponse() {
    // Given
    String prompt = "사용자의 기분을 물어보는 질문을 3가지 방식으로 만들어주세요.";

    // When
    GeminiTextResponse response = geminiApiAdapter.generateText(prompt);

    // Then
    assertNotNull(response);
    assertFalse(response.isEmpty());
    assertFalse(response.getText().isEmpty());
    System.out.println("Gemini API 복잡한 프롬프트 응답: " + response.getText());
  }
}
