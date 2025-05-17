package com.deveagles.be15_deveagles_be.features.chat.config.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketConnectionEventHandler {

  private static final Logger logger =
      LoggerFactory.getLogger(WebSocketConnectionEventHandler.class);

  private final SimpMessageSendingOperations messagingTemplate;
  private final Map<String, String> connectedUsers = new ConcurrentHashMap<>();

  public WebSocketConnectionEventHandler(SimpMessageSendingOperations messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = headerAccessor.getSessionId();

    // TODO : 헤더에서 사용자 정보 추출 (실제 인증 시스템에 맞게 수정 필요)
    String userId = extractUserId(headerAccessor);
    if (userId != null) {
      logger.info("사용자 연결됨: 사용자ID={}, 세션ID={}", userId, sessionId);
      connectedUsers.put(sessionId, userId);

      notifyUserStatusChange(userId, true);
    }
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = headerAccessor.getSessionId();

    String userId = connectedUsers.remove(sessionId);
    if (userId != null) {
      logger.info("사용자 연결 종료: 사용자ID={}, 세션ID={}", userId, sessionId);

      notifyUserStatusChange(userId, false);
    }
  }

  private String extractUserId(StompHeaderAccessor headerAccessor) {
    // TODO: Spring Security 인증 객체에서 정보 추출
    if (headerAccessor.getUser() != null) {
      return headerAccessor.getUser().getName();
    }

    // TODO: 테스트용 임시 코드 (실제 구현 시 제거 필요)
    Object userId = headerAccessor.getSessionAttributes().get("userId");
    return userId != null ? userId.toString() : null;
  }

  private void notifyUserStatusChange(String userId, boolean isOnline) {
    UserStatusMessage statusMessage = new UserStatusMessage(userId, isOnline);

    messagingTemplate.convertAndSend("/topic/status", statusMessage);
    logger.debug("사용자 상태 변경 알림 전송: {}", statusMessage);
  }

  private static class UserStatusMessage {
    private final String userId;
    private final boolean online;
    private final long timestamp;

    public UserStatusMessage(String userId, boolean online) {
      this.userId = userId;
      this.online = online;
      this.timestamp = System.currentTimeMillis();
    }

    public String getUserId() {
      return userId;
    }

    public boolean isOnline() {
      return online;
    }

    public long getTimestamp() {
      return timestamp;
    }

    @Override
    public String toString() {
      return "UserStatusMessage{"
          + "userId='"
          + userId
          + '\''
          + ", online="
          + online
          + ", timestamp="
          + timestamp
          + '}';
    }
  }
}
