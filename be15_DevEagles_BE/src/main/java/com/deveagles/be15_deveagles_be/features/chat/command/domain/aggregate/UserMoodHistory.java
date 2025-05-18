package com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "user_mood_history")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserMoodHistory {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "mood_type")
  private MoodType moodType;

  @Column(name = "intensity")
  private int intensity;

  @Column(name = "inquiry")
  private String inquiry;

  @Column(name = "inquiry_id")
  private String inquiryId;

  @Column(name = "user_answer")
  private String userAnswer;

  @Lob
  @Column(name = "emotion_analysis")
  private String emotionAnalysis;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "answered_at")
  private LocalDateTime answeredAt;

  public enum MoodType {
    JOY,
    SADNESS,
    ANGER,
    FEAR,
    SURPRISE,
    DISGUST,
    NEUTRAL
  }

  public void answer(String answer) {
    this.userAnswer = answer;
    this.answeredAt = LocalDateTime.now();
  }

  public void setEmotionAnalysis(String emotionAnalysis) {
    this.emotionAnalysis = emotionAnalysis;
    analyzeStrongestEmotion();
  }

  private void analyzeStrongestEmotion() {
    if (emotionAnalysis == null || emotionAnalysis.isBlank()) {
      return;
    }

    try {
      List<List<Map<String, Object>>> emotions =
          objectMapper.readValue(
              emotionAnalysis, new TypeReference<List<List<Map<String, Object>>>>() {});

      if (emotions.isEmpty() || emotions.get(0).isEmpty()) {
        return;
      }

      Map<String, Object> strongestEmotion = null;
      double maxScore = -1.0;

      for (Map<String, Object> emotion : emotions.get(0)) {
        String label = (String) emotion.get("label");
        double score = ((Number) emotion.get("score")).doubleValue();

        if (score > maxScore) {
          maxScore = score;
          strongestEmotion = emotion;
        }
      }

      if (strongestEmotion != null) {
        String label = (String) strongestEmotion.get("label");
        double score = ((Number) strongestEmotion.get("score")).doubleValue();

        try {
          this.moodType = MoodType.valueOf(label.toUpperCase());
        } catch (IllegalArgumentException e) {
          this.moodType = MoodType.NEUTRAL;
          log.warn("알 수 없는 감정 라벨: {}, NEUTRAL로 설정됨", label);
        }

        this.intensity = (int) (score * 100);
      }
    } catch (JsonProcessingException e) {
      log.error("감정 분석 JSON 파싱 실패: {}", e.getMessage());
    } catch (Exception e) {
      log.error("감정 분석 처리 중 오류 발생: {}", e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getEmotionAnalysis() {
    if (emotionAnalysis == null || emotionAnalysis.isBlank()) {
      return new ArrayList<>();
    }

    try {
      List<List<Map<String, Object>>> emotions =
          objectMapper.readValue(
              emotionAnalysis, new TypeReference<List<List<Map<String, Object>>>>() {});

      if (!emotions.isEmpty()) {
        return emotions.get(0);
      }
    } catch (JsonProcessingException e) {
      log.error("감정 분석 JSON 파싱 실패: {}", e.getMessage());
    }

    return new ArrayList<>();
  }
}
