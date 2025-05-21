package com.deveagles.be15_deveagles_be.features.chat.config.websocket;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.UserStatusMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl.MoodInquiryServiceImpl;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl.WebSocketMessageService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
@RequiredArgsConstructor
public class WebSocketEventHandler {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketEventHandler.class);

  private final WebSocketMessageService webSocketMessageService;
  private final MoodInquiryServiceImpl moodInquiryService;
  private final Map<String, String> connectedUsers = new ConcurrentHashMap<>();

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = headerAccessor.getSessionId();
    logger.info("웹소켓 연결 요청: 세션 ID={}", sessionId);

    if (headerAccessor.getUser() != null) {
      String username = headerAccessor.getUser().getName();
      logger.info("인증된 사용자 연결 요청: 사용자={}, 세션={}", username, sessionId);
    }
  }

  @EventListener
  public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = headerAccessor.getSessionId();

    String userId = extractUserId(headerAccessor);
    if (userId != null) {
      logger.info("사용자 연결 완료: 사용자ID={}, 세션ID={}", userId, sessionId);

      if (!connectedUsers.containsValue(userId)) {
        try {
          logger.info("첫 로그인 감지: 사용자ID={}, 기분 질문 시도", userId);
          moodInquiryService.sendMoodInquiryOnUserLogin(userId);
        } catch (Exception e) {
          logger.error("기분 질문 전송 중 오류 발생: 사용자ID={}, 오류={}", userId, e.getMessage());
        }
      }

      connectedUsers.put(sessionId, userId);
      notifyUserStatusChange(userId, true);
    }
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = headerAccessor.getSessionId();
    logger.info("웹소켓 연결 해제: 세션 ID={}", sessionId);

    String userId = connectedUsers.remove(sessionId);
    if (userId != null) {
      logger.info("사용자 연결 종료: 사용자ID={}, 세션ID={}", userId, sessionId);

      boolean userStillConnected = connectedUsers.values().contains(userId);

      if (!userStillConnected) {
        notifyUserStatusChange(userId, false);
      }
    }
  }

  private String extractUserId(StompHeaderAccessor headerAccessor) {
    if (headerAccessor.getUser() != null) {
      return headerAccessor.getUser().getName();
    }
    return null;
  }

  private void notifyUserStatusChange(String userId, boolean isOnline) {
    UserStatusMessage statusMessage = new UserStatusMessage(userId, isOnline);
    webSocketMessageService.sendUserStatusEvent(statusMessage);
    logger.debug("사용자 상태 변경 알림 전송: {}", statusMessage);
  }
}
