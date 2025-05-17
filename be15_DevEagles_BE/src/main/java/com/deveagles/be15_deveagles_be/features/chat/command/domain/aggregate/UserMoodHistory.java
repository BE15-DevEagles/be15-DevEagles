package com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_mood_history")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserMoodHistory {

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

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "answered_at")
  private LocalDateTime answeredAt;

  public enum MoodType {
    HAPPY,
    SAD,
    ANGRY,
    TIRED,
    EXCITED,
    ANXIOUS,
    NEUTRAL
  }

  public void answer(String answer) {
    this.userAnswer = answer;
    this.answeredAt = LocalDateTime.now();
  }
}
