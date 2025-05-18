package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HuggingFaceApiAdapter {
  private static final Logger log = LoggerFactory.getLogger(HuggingFaceApiAdapter.class);

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Value("${huggingface.api.key}")
  private String apiKey;

  @Value("${huggingface.api.url}")
  private String apiUrl;

  public HuggingFaceApiAdapter() {
    this.restTemplate = new RestTemplate();
    this.objectMapper = new ObjectMapper();
  }

  public EmotionAnalysisResponse analyzeEmotion(String text) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", "Bearer " + apiKey);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("inputs", text);

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

      log.info("HuggingFace API 호출: {}", apiUrl);
      ResponseEntity<Object> response = restTemplate.postForEntity(apiUrl, entity, Object.class);

      Object responseBody = response.getBody();
      log.info(
          "HuggingFace API 응답 수신: {}",
          responseBody != null ? responseBody.getClass().getName() : "null");

      String emotionAnalysisJson = objectMapper.writeValueAsString(responseBody);
      log.debug("감정 분석 JSON: {}", emotionAnalysisJson);

      return parseResponse(responseBody, emotionAnalysisJson);
    } catch (Exception e) {
      log.error("HuggingFace 감정 분석 API 호출 실패: {}", e.getMessage(), e);
      return EmotionAnalysisResponse.empty();
    }
  }

  private EmotionAnalysisResponse parseResponse(Object responseBody, String rawJson) {
    try {
      List<EmotionData> emotions = new ArrayList<>();

      if (responseBody instanceof List) {
        List<?> outerList = (List<?>) responseBody;
        if (!outerList.isEmpty() && outerList.get(0) instanceof List) {
          List<?> innerList = (List<?>) outerList.get(0);

          String topEmotion = "";
          double topScore = 0.0;

          for (Object item : innerList) {
            if (item instanceof Map) {
              Map<?, ?> emotion = (Map<?, ?>) item;
              String label = String.valueOf(emotion.get("label"));
              double score = ((Number) emotion.get("score")).doubleValue();

              emotions.add(new EmotionData(label, score));

              if (score > topScore) {
                topScore = score;
                topEmotion = label;
              }
            }
          }

          if (!topEmotion.isEmpty()) {
            log.info("HuggingFace 감정 분석 결과: {}, 확률: {}", topEmotion, topScore);
            return new EmotionAnalysisResponse(rawJson, emotions, topEmotion, topScore);
          }
        }
      }

      log.warn("HuggingFace API 응답 파싱 실패");
      return EmotionAnalysisResponse.empty();
    } catch (Exception e) {
      log.error("감정 추출 중 오류 발생: {}", e.getMessage());
      return EmotionAnalysisResponse.empty();
    }
  }

  public static class EmotionAnalysisResponse {
    private final String rawJson;
    private final List<EmotionData> emotions;
    private final String dominantEmotion;
    private final double dominantScore;

    public EmotionAnalysisResponse(
        String rawJson, List<EmotionData> emotions, String dominantEmotion, double dominantScore) {
      this.rawJson = rawJson;
      this.emotions = emotions;
      this.dominantEmotion = dominantEmotion;
      this.dominantScore = dominantScore;
    }

    public static EmotionAnalysisResponse empty() {
      return new EmotionAnalysisResponse("", new ArrayList<>(), "", 0.0);
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

    public boolean isEmpty() {
      return dominantEmotion == null || dominantEmotion.isEmpty();
    }
  }

  public static class EmotionData {
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
}
