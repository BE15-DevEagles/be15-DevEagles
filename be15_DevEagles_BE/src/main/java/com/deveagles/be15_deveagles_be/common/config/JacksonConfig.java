package com.deveagles.be15_deveagles_be.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

  private static final String ISO_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    JavaTimeModule javaTimeModule = new JavaTimeModule();

    // LocalDateTime 직렬화/역직렬화 설정
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(ISO_DATE_TIME_PATTERN);
    javaTimeModule.addSerializer(
        LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
    javaTimeModule.addDeserializer(
        LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

    objectMapper.registerModule(javaTimeModule);

    // 날짜를 타임스탬프 형식이 아닌 ISO 문자열로 직렬화
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    return objectMapper;
  }
}
