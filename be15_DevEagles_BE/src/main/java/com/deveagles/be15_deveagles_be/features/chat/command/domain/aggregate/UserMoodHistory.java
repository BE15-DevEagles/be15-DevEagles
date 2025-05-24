package com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "mood_history_id")
  private Long id;

  @Column(name = "user_id", nullable = false)
  @Convert(converter = StringToLongConverter.class)
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

  /** 사용자 답변을 설정하고 답변 시간을 기록 */
  public void answer(String answer) {
    this.userAnswer = answer;
    this.answeredAt = LocalDateTime.now();
  }

  /** 감정 분석 결과를 설정 */
  public void setEmotionAnalysis(String emotionAnalysis) {
    this.emotionAnalysis = emotionAnalysis;
  }

  /** 기분 유형과 강도를 업데이트 (서비스 레이어에서 호출) */
  public void updateMoodTypeAndIntensity(MoodType moodType, int intensity) {
    this.moodType = moodType;
    this.intensity = intensity;
  }

  @Converter
  public static class StringToLongConverter implements AttributeConverter<String, Long> {
    @Override
    public Long convertToDatabaseColumn(String attribute) {
      if (attribute == null || attribute.isEmpty()) {
        return null;
      }
      try {
        return Long.parseLong(attribute);
      } catch (NumberFormatException e) {
        log.error("String을 Long으로 변환 실패: {}", attribute, e);
        return null;
      }
    }

    @Override
    public String convertToEntityAttribute(Long dbData) {
      return dbData != null ? dbData.toString() : null;
    }
  }
}
