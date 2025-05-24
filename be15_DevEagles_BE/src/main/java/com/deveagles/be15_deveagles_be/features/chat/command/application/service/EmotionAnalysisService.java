package com.deveagles.be15_deveagles_be.features.chat.command.application.service;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory.MoodType;
import java.util.List;

public interface EmotionAnalysisService {

  EmotionAnalysisResult analyzeEmotion(String text);

  List<EmotionData> parseEmotionAnalysisJson(String emotionAnalysisJson);

  StrongestEmotionResult extractStrongestEmotion(String emotionAnalysisJson);

  MoodType convertToMoodType(String emotionLabel);

  class EmotionAnalysisResult {
    private final String rawJson;
    private final List<EmotionData> emotions;
    private final String dominantEmotion;
    private final double dominantScore;
    private final MoodType moodType;
    private final int intensity;

    public EmotionAnalysisResult(
        String rawJson,
        List<EmotionData> emotions,
        String dominantEmotion,
        double dominantScore,
        MoodType moodType,
        int intensity) {
      this.rawJson = rawJson;
      this.emotions = emotions;
      this.dominantEmotion = dominantEmotion;
      this.dominantScore = dominantScore;
      this.moodType = moodType;
      this.intensity = intensity;
    }

    public String getRawJson() {
      return rawJson;
    }

    public List<EmotionData> getEmotions() {
      return emotions;
    }

    public String getDominantEmotion() {
      return dominantEmotion;
    }

    public double getDominantScore() {
      return dominantScore;
    }

    public MoodType getMoodType() {
      return moodType;
    }

    public int getIntensity() {
      return intensity;
    }

    public boolean isEmpty() {
      return dominantEmotion == null || dominantEmotion.isEmpty();
    }
  }

  /** 개별 감정 데이터 클래스 */
  class EmotionData {
    private final String label;
    private final double score;

    public EmotionData(String label, double score) {
      this.label = label;
      this.score = score;
    }

    public String getLabel() {
      return label;
    }

    public double getScore() {
      return score;
    }
  }

  class StrongestEmotionResult {
    private final MoodType moodType;
    private final int intensity;
    private final String emotionLabel;
    private final double score;

    public StrongestEmotionResult(
        MoodType moodType, int intensity, String emotionLabel, double score) {
      this.moodType = moodType;
      this.intensity = intensity;
      this.emotionLabel = emotionLabel;
      this.score = score;
    }

    public MoodType getMoodType() {
      return moodType;
    }

    public int getIntensity() {
      return intensity;
    }

    public String getEmotionLabel() {
      return emotionLabel;
    }

    public double getScore() {
      return score;
    }
  }
}
