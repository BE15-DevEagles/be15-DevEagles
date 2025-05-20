package com.deveagles.be15_deveagles_be.features.auth.command.application.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RedisTemplate<String, String> redisTemplate;

  @Value("${jwt.refresh-expiration}")
  private long jwtRefreshExpiration;

  @Override
  public void saveRefreshToken(String username, String refreshToken) {

    ValueOperations<String, String> values = redisTemplate.opsForValue();
    values.set("RT:" + username, refreshToken, Duration.ofMillis(jwtRefreshExpiration));
  }

  @Override
  public String getRefreshToken(String username) {
    return redisTemplate.opsForValue().get("RT:" + username);
  }

  @Override
  public void deleteRefreshToken(String username) {
    redisTemplate.delete("RT:" + username);
  }
}
