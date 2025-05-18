package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiApiAdapter {
  private static final Logger log = LoggerFactory.getLogger(GeminiApiAdapter.class);

  private final RestTemplate restTemplate;

  @Value("${gemini.api.key}")
  private String apiKey;

  @Value("${gemini.api.url}")
  private String apiUrl;

  @Value("${gemini.api.timeout:5000}")
  private int apiTimeoutMs;

  public GeminiApiAdapter() {
    this.restTemplate = createRestTemplateWithTimeout();
  }

  public GeminiTextResponse generateText(String prompt) {
    try {
      Map<String, Object> request = createRequestBody(prompt);

      String apiUrlWithKey = apiUrl + "?key=" + apiKey;
      log.debug("Gemini API 호출: {}", apiUrlWithKey);

      Map<String, Object> response = restTemplate.postForObject(apiUrlWithKey, request, Map.class);
      log.debug("Gemini API 응답 수신됨");

      return parseResponse(response);
    } catch (Exception e) {
      log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
      return GeminiTextResponse.empty();
    }
  }

  private Map<String, Object> createRequestBody(String prompt) {
    Map<String, Object> request = new HashMap<>();
    Map<String, Object> content = new HashMap<>();
    Map<String, Object> part = new HashMap<>();

    part.put("text", prompt);
    content.put("parts", List.of(part));
    request.put("contents", List.of(content));

    Map<String, Object> generationConfig = new HashMap<>();
    generationConfig.put("temperature", 0.7);
    generationConfig.put("maxOutputTokens", 200);
    request.put("generationConfig", generationConfig);

    return request;
  }

  private GeminiTextResponse parseResponse(Map<String, Object> response) {
    try {
      if (response != null && response.containsKey("candidates")) {
        List<Map<String, Object>> candidates =
            (List<Map<String, Object>>) response.get("candidates");
        if (candidates != null && !candidates.isEmpty()) {
          Map<String, Object> candidate = candidates.get(0);
          if (candidate != null && candidate.containsKey("content")) {
            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
            if (content != null && content.containsKey("parts")) {
              List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
              if (parts != null
                  && !parts.isEmpty()
                  && parts.get(0) != null
                  && parts.get(0).containsKey("text")) {
                String text = (String) parts.get(0).get("text");
                if (text != null && !text.trim().isEmpty()) {
                  double confidence = 1.0;
                  if (candidate.containsKey("confidence")) {
                    confidence = ((Number) candidate.get("confidence")).doubleValue();
                  }

                  return new GeminiTextResponse(text, confidence, response);
                }
              }
            }
          }
        }
        log.warn("API 응답에서 텍스트를 추출할 수 없음");
      } else {
        log.warn("API 응답에 candidates가 없거나 응답이 null임");
      }
    } catch (Exception e) {
      log.error("API 응답 처리 중 예외 발생: {}", e.getMessage(), e);
    }
    return GeminiTextResponse.empty();
  }

  private RestTemplate createRestTemplateWithTimeout() {
    RestTemplate template = new RestTemplate();
    template.setRequestFactory(new SimpleClientHttpRequestFactory());
    ((SimpleClientHttpRequestFactory) template.getRequestFactory()).setConnectTimeout(apiTimeoutMs);
    ((SimpleClientHttpRequestFactory) template.getRequestFactory()).setReadTimeout(apiTimeoutMs);
    return template;
  }

  public static class GeminiTextResponse {
    private final String text;
    private final double confidence;
    private final Map<String, Object> rawResponse;

    public GeminiTextResponse(String text, double confidence, Map<String, Object> rawResponse) {
      this.text = text;
      this.confidence = confidence;
      this.rawResponse = rawResponse;
    }

    public static GeminiTextResponse empty() {
      return new GeminiTextResponse("", 0.0, Map.of());
    }

    public String getText() {
      return text;
    }

    public double getConfidence() {
      return confidence;
    }

    public Map<String, Object> getRawResponse() {
      return rawResponse;
    }

    public boolean isEmpty() {
      return text == null || text.trim().isEmpty();
    }
  }
}
