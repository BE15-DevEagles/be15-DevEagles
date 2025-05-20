package com.deveagles.be15_deveagles_be.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration}")
  private long jwtExpiration;

  @Value("${jwt.refresh-expiration}")
  private long jwtRefreshExpiration;

  private SecretKey secretKey;

  private final RedisTemplate<String, String> redisTemplate;

  @PostConstruct
  public void init() {

    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    secretKey = Keys.hmacShaKeyFor(keyBytes);
  }

  public String createToken(String username) {

    Date now = new Date();
    Date expiration = new Date(now.getTime() + jwtExpiration);

    return Jwts.builder()
        .subject(username)
        .issuedAt(now)
        .claim("type", "access")
        .expiration(expiration)
        .signWith(secretKey)
        .compact();
  }

  public String createRefreshToken(String username) {

    Date now = new Date();
    Date expiration = new Date(now.getTime() + jwtRefreshExpiration);

    return Jwts.builder()
        .subject(username)
        .issuedAt(now)
        .claim("type", "refresh")
        .expiration(expiration)
        .signWith(secretKey)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      throw new BadCredentialsException("Invalid JWT Token", e);
    } catch (ExpiredJwtException e) {
      throw new BadCredentialsException("Expired JWT Token", e);
    } catch (UnsupportedJwtException e) {
      throw new BadCredentialsException("Unsupported JWT Token", e);
    } catch (IllegalArgumentException e) {
      throw new BadCredentialsException("JWT Token claims empty", e);
    }
  }

  public String getUsernameFromJWT(String token) {
    Claims claims =
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    return claims.getSubject();
  }

  public long getRemainingExpiration(String token) {
    Claims claims = parseClaims(token);
    return claims.getExpiration().getTime() - System.currentTimeMillis();
  }

  public Claims parseClaims(String token) {
    try {
      return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      return e.getClaims(); // 만료된 토큰도 claims는 꺼낼 수 있음
    }
  }

  public boolean isRefreshToken(String token) {
    Claims claims = parseClaims(token);
    return "refresh".equals(claims.get("type", String.class));
  }

  public boolean isAccessTokenBlacklisted(String token) {
    return Boolean.TRUE.equals(redisTemplate.hasKey("BL:" + token));
  }
}
