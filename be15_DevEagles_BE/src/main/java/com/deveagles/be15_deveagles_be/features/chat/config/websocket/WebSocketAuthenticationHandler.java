package com.deveagles.be15_deveagles_be.features.chat.config.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
public class WebSocketAuthenticationHandler {

  private static final Logger logger =
      LoggerFactory.getLogger(WebSocketAuthenticationHandler.class);

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = headerAccessor.getSessionId();
    logger.info("웹소켓 연결: 세션 ID={}", sessionId);

    // TODO: 사용자 인증 정보 추가되는 거 확인해서 수정
    if (headerAccessor.getUser() != null) {
      String username = headerAccessor.getUser().getName();
      logger.info("인증된 사용자 연결: 사용자={}, 세션={}", username, sessionId);
    }
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = headerAccessor.getSessionId();
    logger.info("웹소켓 연결 해제: 세션 ID={}", sessionId);

    // TODO: 사용자 인증 정보 추가되는 거 확인해서 수정
    if (headerAccessor.getUser() != null) {
      String username = headerAccessor.getUser().getName();
      logger.info("인증된 사용자 연결 해제: 사용자={}, 세션={}", username, sessionId);
    }
  }
}
