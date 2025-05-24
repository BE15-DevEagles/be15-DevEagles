package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.service.EmotionAnalysisService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory.MoodType;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.HuggingFaceApiAdapter;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.HuggingFaceApiAdapter.EmotionAnalysisResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmotionAnalysisServiceImpl implements EmotionAnalysisService {

  private static final Logger log = LoggerFactory.getLogger(EmotionAnalysisServiceImpl.class);

  private final HuggingFaceApiAdapter huggingFaceApiAdapter;
  private final ObjectMapper objectMapper;

  public EmotionAnalysisServiceImpl(
      HuggingFaceApiAdapter huggingFaceApiAdapter, ObjectMapper objectMapper) {
    this.huggingFaceApiAdapter = huggingFaceApiAdapter;
    this.objectMapper = objectMapper;
  }

  @Override
  public EmotionAnalysisResult analyzeEmotion(String text) {
    if (text == null || text.trim().isEmpty()) {
      return createEmptyResult();
    }

    try {
      EmotionAnalysisResponse apiResponse = huggingFaceApiAdapter.analyzeEmotion(text);

      if (apiResponse.isEmpty()) {
        return createEmptyResult();
      }

      List<EmotionData> emotions = convertToEmotionDataList(apiResponse.getEmotions());
      MoodType moodType = convertToMoodType(apiResponse.getDominantEmotion());
      int intensity = (int) (apiResponse.getDominantScore() * 100);

      return new EmotionAnalysisResult(
          apiResponse.getRawJson(),
          emotions,
          apiResponse.getDominantEmotion(),
          apiResponse.getDominantScore(),
          moodType,
          intensity);
    } catch (Exception e) {
      log.error("감정 분석 중 오류 발생: {}", e.getMessage(), e);
      return createEmptyResult();
    }
  }

  @Override
  public List<EmotionData> parseEmotionAnalysisJson(String emotionAnalysisJson) {
    if (emotionAnalysisJson == null || emotionAnalysisJson.isBlank()) {
      return new ArrayList<>();
    }

    try {
      List<List<Map<String, Object>>> emotions =
          objectMapper.readValue(
              emotionAnalysisJson, new TypeReference<List<List<Map<String, Object>>>>() {});

      if (emotions.isEmpty() || emotions.get(0).isEmpty()) {
        return new ArrayList<>();
      }

      List<EmotionData> result = new ArrayList<>();
      for (Map<String, Object> emotion : emotions.get(0)) {
        String label = (String) emotion.get("label");
        double score = ((Number) emotion.get("score")).doubleValue();
        result.add(new EmotionData(label, score));
      }

      return result;
    } catch (JsonProcessingException e) {
      log.error("감정 분석 JSON 파싱 실패: {}", e.getMessage());
      return new ArrayList<>();
    } catch (Exception e) {
      log.error("감정 분석 JSON 처리 중 오류 발생: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  @Override
  public StrongestEmotionResult extractStrongestEmotion(String emotionAnalysisJson) {
    if (emotionAnalysisJson == null || emotionAnalysisJson.isBlank()) {
      return new StrongestEmotionResult(MoodType.NEUTRAL, 0, "NEUTRAL", 0.0);
    }

    try {
      List<List<Map<String, Object>>> emotions =
          objectMapper.readValue(
              emotionAnalysisJson, new TypeReference<List<List<Map<String, Object>>>>() {});

      if (emotions.isEmpty() || emotions.get(0).isEmpty()) {
        return new StrongestEmotionResult(MoodType.NEUTRAL, 0, "NEUTRAL", 0.0);
      }

      Map<String, Object> strongestEmotion = null;
      double maxScore = -1.0;

      for (Map<String, Object> emotion : emotions.get(0)) {
        double score = ((Number) emotion.get("score")).doubleValue();
        if (score > maxScore) {
          maxScore = score;
          strongestEmotion = emotion;
        }
      }

      if (strongestEmotion != null) {
        String label = (String) strongestEmotion.get("label");
        double score = ((Number) strongestEmotion.get("score")).doubleValue();
        MoodType moodType = convertToMoodType(label);
        int intensity = (int) (score * 100);

        return new StrongestEmotionResult(moodType, intensity, label, score);
      }

    } catch (JsonProcessingException e) {
      log.error("감정 분석 JSON 파싱 실패: {}", e.getMessage());
    } catch (Exception e) {
      log.error("감정 분석 처리 중 오류 발생: {}", e.getMessage());
    }

    return new StrongestEmotionResult(MoodType.NEUTRAL, 0, "NEUTRAL", 0.0);
  }

  @Override
  public MoodType convertToMoodType(String emotionLabel) {
    if (emotionLabel == null || emotionLabel.trim().isEmpty()) {
      return MoodType.NEUTRAL;
    }

    try {
      return MoodType.valueOf(emotionLabel.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("알 수 없는 감정 라벨: {}, NEUTRAL로 설정됨", emotionLabel);
      return MoodType.NEUTRAL;
    }
  }

  private EmotionAnalysisResult createEmptyResult() {
    return new EmotionAnalysisResult("", new ArrayList<>(), "", 0.0, MoodType.NEUTRAL, 0);
  }

  private List<EmotionData> convertToEmotionDataList(
      List<HuggingFaceApiAdapter.EmotionData> apiEmotions) {
    List<EmotionData> result = new ArrayList<>();
    for (HuggingFaceApiAdapter.EmotionData emotion : apiEmotions) {
      result.add(new EmotionData(emotion.getLabel(), emotion.getScore()));
    }
    return result;
  }
}
