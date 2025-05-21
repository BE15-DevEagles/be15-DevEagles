package com.deveagles.be15_deveagles_be.features.auth.command.application.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthCodeService {

  private final RedisTemplate<String, String> redisTemplate;

  @Value("${spring.mail.properties.auth-code-expiration-millis}")
  private long expireMinute;

  public void saveAuthCode(String email, String code) {
    redisTemplate.opsForValue().set(email, code, Duration.ofMillis(expireMinute));
  }

  public String getAuthCode(String email) {
    return redisTemplate.opsForValue().get(email);
  }

  public void deleteAuthCode(String email) {
    redisTemplate.delete(email);
  }
}
