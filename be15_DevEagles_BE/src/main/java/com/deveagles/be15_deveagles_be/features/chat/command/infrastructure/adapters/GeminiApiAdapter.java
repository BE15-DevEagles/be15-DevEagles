package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.GeminiApiException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiApiAdapter {
  private static final Logger log = LoggerFactory.getLogger(GeminiApiAdapter.class);
  private static final int DEFAULT_MAX_TOKENS = 200;
  private static final double DEFAULT_TEMPERATURE = 0.7;

  private final RestTemplate restTemplate;
  private final GeminiRequestBuilder requestBuilder;
  private final GeminiResponseParser responseParser;

  @Value("${gemini.api.key}")
  private String apiKey;

  @Value("${gemini.api.url}")
  private String apiUrl;

  @Value("${gemini.api.timeout:5000}")
  private int apiTimeoutMs;

  @Autowired
  public GeminiApiAdapter(RestTemplate restTemplate) {
    this.restTemplate = configureRestTemplate(restTemplate);
    this.requestBuilder = new GeminiRequestBuilder();
    this.responseParser = new GeminiResponseParser();
  }

  private RestTemplate configureRestTemplate(RestTemplate template) {
    if (template.getRequestFactory() instanceof SimpleClientHttpRequestFactory) {
      SimpleClientHttpRequestFactory factory =
          (SimpleClientHttpRequestFactory) template.getRequestFactory();
      factory.setConnectTimeout(apiTimeoutMs);
      factory.setReadTimeout(apiTimeoutMs);
    }
    return template;
  }

  public GeminiTextResponse generateText(String prompt) {
    return generateText(prompt, DEFAULT_TEMPERATURE, DEFAULT_MAX_TOKENS);
  }

  public GeminiTextResponse generateText(String prompt, double temperature, int maxTokens) {
    validatePrompt(prompt);

    try {
      Map<String, Object> request = requestBuilder.buildRequest(prompt, temperature, maxTokens);
      String apiUrlWithKey = apiUrl + "?key=" + apiKey;
      log.debug("Gemini API 호출: {}", apiUrlWithKey);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

      ResponseEntity<Map> responseEntity =
          restTemplate.exchange(apiUrlWithKey, HttpMethod.POST, entity, Map.class);

      log.debug("Gemini API 응답 수신됨 (상태 코드: {})", responseEntity.getStatusCode());

      if (!responseEntity.getStatusCode().is2xxSuccessful()) {
        throw new GeminiApiException.ServerException(
            "Gemini API 응답 오류: " + responseEntity.getStatusCode(),
            "API_ERROR_" + responseEntity.getStatusCode().value());
      }

      return responseParser.parse(responseEntity.getBody());
    } catch (HttpClientErrorException e) {
      log.error("Gemini API 클라이언트 오류: {}", e.getMessage());
      throw new GeminiApiException.ClientException(
          "Gemini API 요청 오류: " + e.getMessage(), "CLIENT_ERROR_" + e.getStatusCode().value());
    } catch (HttpServerErrorException e) {
      log.error("Gemini API 서버 오류: {}", e.getMessage());
      throw new GeminiApiException.ServerException(
          "Gemini API 서버 오류: " + e.getMessage(), "SERVER_ERROR_" + e.getStatusCode().value());
    } catch (ResourceAccessException e) {
      log.error("Gemini API 연결 오류: {}", e.getMessage());
      throw new GeminiApiException.ConnectionException(
          "Gemini API 연결 오류: " + e.getMessage(), "CONNECTION_ERROR");
    } catch (GeminiApiException e) {
      throw e;
    } catch (Exception e) {
      log.error("Gemini API 호출 중 예기치 않은 오류 발생: {}", e.getMessage(), e);
      throw new GeminiApiException.UnexpectedException(
          "Gemini API 호출 중 예기치 않은 오류: " + e.getMessage(), "UNEXPECTED_ERROR");
    }
  }

  private void validatePrompt(String prompt) {
    if (prompt == null || prompt.trim().isEmpty()) {
      throw new GeminiApiException.ValidationException("프롬프트는 비어있을 수 없습니다", "EMPTY_PROMPT");
    }
  }

  private static class GeminiRequestBuilder {
    public Map<String, Object> buildRequest(String prompt, double temperature, int maxTokens) {
      Map<String, Object> request = new HashMap<>();

      Map<String, Object> part = new HashMap<>();
      part.put("text", prompt);

      Map<String, Object> content = new HashMap<>();
      content.put("parts", List.of(part));

      request.put("contents", List.of(content));

      Map<String, Object> generationConfig = new HashMap<>();
      generationConfig.put("temperature", temperature);
      generationConfig.put("maxOutputTokens", maxTokens);
      request.put("generationConfig", generationConfig);

      return request;
    }
  }

  private static class GeminiResponseParser {
    private final Logger log = LoggerFactory.getLogger(GeminiResponseParser.class);

    public GeminiTextResponse parse(Map<String, Object> response) {
      if (response == null) {
        throw new GeminiApiException.ParsingException("API 응답이 null입니다", "NULL_RESPONSE");
      }

      try {
        return extractCandidates(response)
            .flatMap(this::extractFirstCandidate)
            .flatMap(this::extractContent)
            .flatMap(this::extractParts)
            .flatMap(this::extractFirstPart)
            .flatMap(this::extractText)
            .map(text -> createResponse(text, extractConfidence(response), response))
            .orElseThrow(
                () ->
                    new GeminiApiException.ParsingException(
                        "API 응답에서 텍스트를 추출할 수 없습니다", "TEXT_EXTRACTION_FAILED"));
      } catch (GeminiApiException e) {
        throw e;
      } catch (Exception e) {
        log.error("API 응답 파싱 중 예외 발생: {}", e.getMessage(), e);
        throw new GeminiApiException.ParsingException(
            "API 응답 파싱 중 예외 발생: " + e.getMessage(), "PARSING_ERROR");
      }
    }

    private Optional<List<Map<String, Object>>> extractCandidates(Map<String, Object> response) {
      return Optional.ofNullable(response)
          .map(r -> (List<Map<String, Object>>) r.get("candidates"));
    }

    private Optional<Map<String, Object>> extractFirstCandidate(
        List<Map<String, Object>> candidates) {
      return Optional.ofNullable(candidates).filter(c -> !c.isEmpty()).map(c -> c.get(0));
    }

    private Optional<Map<String, Object>> extractContent(Map<String, Object> candidate) {
      return Optional.ofNullable(candidate).map(c -> (Map<String, Object>) c.get("content"));
    }

    private Optional<List<Map<String, Object>>> extractParts(Map<String, Object> content) {
      return Optional.ofNullable(content).map(c -> (List<Map<String, Object>>) c.get("parts"));
    }

    private Optional<Map<String, Object>> extractFirstPart(List<Map<String, Object>> parts) {
      return Optional.ofNullable(parts).filter(p -> !p.isEmpty()).map(p -> p.get(0));
    }

    private Optional<String> extractText(Map<String, Object> part) {
      return Optional.ofNullable(part)
          .map(p -> (String) p.get("text"))
          .filter(text -> !text.trim().isEmpty());
    }

    private double extractConfidence(Map<String, Object> response) {
      return extractCandidates(response)
          .flatMap(this::extractFirstCandidate)
          .map(c -> c.get("confidence"))
          .filter(Number.class::isInstance)
          .map(Number.class::cast)
          .map(Number::doubleValue)
          .orElse(1.0);
    }

    private GeminiTextResponse createResponse(
        String text, double confidence, Map<String, Object> rawResponse) {
      return new GeminiTextResponse(text, confidence, rawResponse);
    }
  }

  public static class GeminiTextResponse {
    private final String text;
    private final double confidence;
    private final Map<String, Object> rawResponse;

    public GeminiTextResponse(String text, double confidence, Map<String, Object> rawResponse) {
      this.text = text;
      this.confidence = confidence;
      this.rawResponse = Collections.unmodifiableMap(rawResponse);
    }

    public static GeminiTextResponse empty() {
      return new GeminiTextResponse("", 0.0, Collections.emptyMap());
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
