package com.deveagles.be15_deveagles_be.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = getJwtFromRequest(request);
    log.info("## user login -> token: {}", token);
    try {
      if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

        if (jwtTokenProvider.isRefreshToken(token)) {
          log.warn("# refreshToken으로 접근 시도 차단");
          response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED, "refreshToken은 API 인증에 사용할 수 없습니다.");
          return;
        }

        if (Boolean.TRUE.equals(jwtTokenProvider.isAccessTokenBlacklisted(token))) {
          log.warn("# 블랙리스트 토큰 사용 시도");
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그아웃된 토큰입니다.");
          return;
        }

        String username = jwtTokenProvider.getUsernameFromJWT(token);
        log.info("## user login -> username: {}", username);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (Exception e) {
      log.error("# JWT 처리 중 에러 발생", e);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰 오류");
      return;
    }
    filterChain.doFilter(request, response);
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
