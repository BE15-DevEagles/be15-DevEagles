package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.HuggingFaceApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class HuggingFaceApiAdapter {
  private static final Logger log = LoggerFactory.getLogger(HuggingFaceApiAdapter.class);
  private static final int DEFAULT_TIMEOUT_MS = 5000;

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final HuggingFaceRequestBuilder requestBuilder;
  private final HuggingFaceResponseParser responseParser;

  @Value("${huggingface.api.key}")
  private String apiKey;

  @Value("${huggingface.api.url}")
  private String apiUrl;

  @Value("${huggingface.api.timeout:5000}")
  private int apiTimeoutMs;

  @Autowired
  public HuggingFaceApiAdapter(RestTemplate restTemplate, ObjectMapper objectMapper) {
    this.restTemplate = configureRestTemplate(restTemplate, objectMapper);
    this.objectMapper = objectMapper;
    this.requestBuilder = new HuggingFaceRequestBuilder();
    this.responseParser = new HuggingFaceResponseParser(objectMapper);
  }

  private RestTemplate configureRestTemplate(RestTemplate template, ObjectMapper objectMapper) {
    MappingJackson2HttpMessageConverter messageConverter =
        new MappingJackson2HttpMessageConverter();
    messageConverter.setObjectMapper(objectMapper);
    template.getMessageConverters().add(0, messageConverter);

    if (template.getRequestFactory() instanceof SimpleClientHttpRequestFactory) {
      SimpleClientHttpRequestFactory factory =
          (SimpleClientHttpRequestFactory) template.getRequestFactory();
      factory.setConnectTimeout(apiTimeoutMs);
      factory.setReadTimeout(apiTimeoutMs);
    }

    return template;
  }

  public EmotionAnalysisResponse analyzeEmotion(String text) {
    validateInput(text);

    try {
      Map<String, Object> requestBody = requestBuilder.buildRequest(text);
      HttpHeaders headers = createHeaders();
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

      log.info("HuggingFace API 호출: {}", apiUrl);

      try {
        ResponseEntity<Object> response = restTemplate.postForEntity(apiUrl, entity, Object.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
          throw new HuggingFaceApiException.ServerException(
              "HuggingFace API 응답 오류: " + response.getStatusCode(),
              "API_ERROR_" + response.getStatusCode().value());
        }

        Object responseBody = response.getBody();
        log.info(
            "HuggingFace API 응답 수신: {}",
            responseBody != null ? responseBody.getClass().getName() : "null");

        String emotionAnalysisJson = objectMapper.writeValueAsString(responseBody);
        log.debug("감정 분석 JSON: {}", emotionAnalysisJson);

        return responseParser.parse(responseBody, emotionAnalysisJson);
      } catch (org.springframework.http.InvalidMediaTypeException e) {
        log.warn("응답의 Content-Type 헤더 파싱 중 오류 발생: {}", e.getMessage());

        Object responseBody = restTemplate.postForObject(apiUrl, entity, Object.class);

        if (responseBody != null) {
          String emotionAnalysisJson = objectMapper.writeValueAsString(responseBody);
          log.debug("대체 방법을 통한 감정 분석 JSON: {}", emotionAnalysisJson);
          return responseParser.parse(responseBody, emotionAnalysisJson);
        }

        throw new HuggingFaceApiException.ParsingException(
            "응답의 Content-Type 헤더 파싱 중 오류: " + e.getMessage(), "INVALID_MEDIA_TYPE");
      }
    } catch (HttpClientErrorException e) {
      log.error("HuggingFace API 클라이언트 오류: {}", e.getMessage());
      throw new HuggingFaceApiException.ClientException(
          "HuggingFace API 요청 오류: " + e.getMessage(), "CLIENT_ERROR_" + e.getStatusCode().value());
    } catch (HttpServerErrorException e) {
      log.error("HuggingFace API 서버 오류: {}", e.getMessage());
      throw new HuggingFaceApiException.ServerException(
          "HuggingFace API 서버 오류: " + e.getMessage(), "SERVER_ERROR_" + e.getStatusCode().value());
    } catch (ResourceAccessException e) {
      log.error("HuggingFace API 연결 오류: {}", e.getMessage());
      throw new HuggingFaceApiException.ConnectionException(
          "HuggingFace API 연결 오류: " + e.getMessage(), "CONNECTION_ERROR");
    } catch (HuggingFaceApiException e) {
      throw e;
    } catch (Exception e) {
      log.error("HuggingFace 감정 분석 API 호출 실패: {}", e.getMessage(), e);
      throw new HuggingFaceApiException.UnexpectedException(
          "HuggingFace API 호출 중 예기치 않은 오류: " + e.getMessage(), "UNEXPECTED_ERROR");
    }
  }

  private void validateInput(String text) {
    if (text == null || text.trim().isEmpty()) {
      throw new HuggingFaceApiException.ValidationException("분석할 텍스트는 비어있을 수 없습니다", "EMPTY_TEXT");
    }
  }

  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    headers.set("Authorization", "Bearer " + apiKey);
    return headers;
  }

  private static class HuggingFaceRequestBuilder {
    public Map<String, Object> buildRequest(String text) {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("inputs", text);
      return requestBody;
    }
  }

  private static class HuggingFaceResponseParser {
    private final Logger log = LoggerFactory.getLogger(HuggingFaceResponseParser.class);
    private final ObjectMapper objectMapper;

    public HuggingFaceResponseParser(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
    }

    public EmotionAnalysisResponse parse(Object responseBody, String rawJson) {
      if (responseBody == null) {
        throw new HuggingFaceApiException.ParsingException("응답 본문이 null입니다", "NULL_RESPONSE");
      }

      try {
        return parseResponseData(responseBody, rawJson)
            .orElseThrow(
                () ->
                    new HuggingFaceApiException.ParsingException(
                        "감정 분석 결과를 추출할 수 없습니다", "EMOTION_EXTRACTION_FAILED"));
      } catch (HuggingFaceApiException e) {
        throw e;
      } catch (Exception e) {
        log.error("감정 추출 중 오류 발생: {}", e.getMessage());
        throw new HuggingFaceApiException.ParsingException(
            "감정 추출 중 오류 발생: " + e.getMessage(), "PARSING_ERROR");
      }
    }

    private Optional<EmotionAnalysisResponse> parseResponseData(
        Object responseBody, String rawJson) {
      if (!(responseBody instanceof List<?>)) {
        return Optional.empty();
      }

      List<?> outerList = (List<?>) responseBody;
      if (outerList.isEmpty() || !(outerList.get(0) instanceof List<?>)) {
        return Optional.empty();
      }

      List<?> innerList = (List<?>) outerList.get(0);
      List<EmotionData> emotions = new ArrayList<>();
      String topEmotion = "";
      double topScore = 0.0;

      for (Object item : innerList) {
        if (item instanceof Map<?, ?> emotion) {
          Optional<EmotionData> extractedEmotion = extractEmotionData(emotion);

          extractedEmotion.ifPresent(
              data -> {
                emotions.add(data);
              });

          if (extractedEmotion.isPresent() && extractedEmotion.get().getScore() > topScore) {
            EmotionData data = extractedEmotion.get();
            topScore = data.getScore();
            topEmotion = data.getLabel();
          }
        }
      }

      if (!topEmotion.isEmpty()) {
        log.info("HuggingFace 감정 분석 결과: {}, 확률: {}", topEmotion, topScore);
        return Optional.of(new EmotionAnalysisResponse(rawJson, emotions, topEmotion, topScore));
      }

      return Optional.empty();
    }

    private Optional<EmotionData> extractEmotionData(Map<?, ?> emotion) {
      try {
        String label = String.valueOf(emotion.get("label"));
        double score = ((Number) emotion.get("score")).doubleValue();
        return Optional.of(new EmotionData(label, score));
      } catch (Exception e) {
        log.warn("감정 데이터 추출 실패: {}", e.getMessage());
        return Optional.empty();
      }
    }
  }

  @Getter
  public static class EmotionAnalysisResponse {
    private final String rawJson;
    private final List<EmotionData> emotions;
    private final String dominantEmotion;
    private final double dominantScore;

    public EmotionAnalysisResponse(
        String rawJson, List<EmotionData> emotions, String dominantEmotion, double dominantScore) {
      this.rawJson = rawJson;
      this.emotions = Collections.unmodifiableList(new ArrayList<>(emotions));
      this.dominantEmotion = dominantEmotion;
      this.dominantScore = dominantScore;
    }

    public static EmotionAnalysisResponse empty() {
      return new EmotionAnalysisResponse("", Collections.emptyList(), "", 0.0);
    }

    public boolean isEmpty() {
      return dominantEmotion == null || dominantEmotion.isEmpty();
    }
  }

  @Getter
  public static class EmotionData {
    private final String label;
    private final double score;

    public EmotionData(String label, double score) {
      this.label = label;
      this.score = score;
    }
  }
}
