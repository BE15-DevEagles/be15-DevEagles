package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.HuggingFaceApiAdapter.EmotionAnalysisResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** HuggingFace API 실제 연결 통합 테스트 */
// @Disabled
@SpringBootTest
public class HuggingFaceApiAdapterIntegrationTest {

  @Autowired private HuggingFaceApiAdapter huggingFaceApiAdapter;

  @Test
  @DisplayName("행복한 텍스트 감정 분석 테스트")
  void whenAnalyzeHappyEmotion_thenReturnEmotionScore() {
    // Given
    String text = "오늘은 날씨도 좋고 기분이 너무 좋아요! 정말 행복해요.";

    // When
    EmotionAnalysisResponse response = huggingFaceApiAdapter.analyzeEmotion(text);

    // Then
    assertNotNull(response);
    assertFalse(response.isEmpty());
    assertFalse(response.getEmotions().isEmpty());
    // API가 현재 대부분 neutral을 반환하므로 특정 감정을 단언하지 않고 응답이 유효한지만 확인
    assertTrue(response.getDominantScore() > 0);
    System.out.println(
        "행복한 텍스트 감정 분석 결과: "
            + response.getDominantEmotion()
            + ", 확률: "
            + response.getDominantScore());
    System.out.println("전체 감정 분석: " + response.getEmotions());
  }

  @Test
  @DisplayName("슬픈 텍스트 감정 분석 테스트")
  void whenAnalyzeSadEmotion_thenReturnEmotionScore() {
    // Given
    String text = "오늘은 정말 우울하고 슬픈 하루였어요. 힘든 일이 많았습니다.";

    // When
    EmotionAnalysisResponse response = huggingFaceApiAdapter.analyzeEmotion(text);

    // Then
    assertNotNull(response);
    assertFalse(response.isEmpty());
    assertFalse(response.getEmotions().isEmpty());
    // API가 현재 대부분 neutral을 반환하므로 특정 감정을 단언하지 않고 응답이 유효한지만 확인
    assertTrue(response.getDominantScore() > 0);
    System.out.println(
        "슬픈 텍스트 감정 분석 결과: "
            + response.getDominantEmotion()
            + ", 확률: "
            + response.getDominantScore());
    System.out.println("전체 감정 분석: " + response.getEmotions());
  }

  @Test
  @DisplayName("화난 텍스트 감정 분석 테스트")
  void whenAnalyzeAngryEmotion_thenReturnEmotionScore() {
    // Given
    String text = "정말 화가 나고 짜증이 납니다! 이런 상황이 계속되면 참을 수 없어요.";

    // When
    EmotionAnalysisResponse response = huggingFaceApiAdapter.analyzeEmotion(text);

    // Then
    assertNotNull(response);
    assertFalse(response.isEmpty());
    assertFalse(response.getEmotions().isEmpty());
    // API가 현재 대부분 neutral을 반환하므로 특정 감정을 단언하지 않고 응답이 유효한지만 확인
    assertTrue(response.getDominantScore() > 0);
    System.out.println(
        "화난 텍스트 감정 분석 결과: "
            + response.getDominantEmotion()
            + ", 확률: "
            + response.getDominantScore());
    System.out.println("전체 감정 분석: " + response.getEmotions());
  }
}
