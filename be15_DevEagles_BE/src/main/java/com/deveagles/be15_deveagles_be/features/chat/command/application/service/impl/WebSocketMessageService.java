package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageService {

  private final SimpMessagingTemplate messagingTemplate;

  private static final String CHATROOM_TOPIC_FORMAT = "/topic/chatroom.%s";
  private static final String CHATROOM_DELETE_TOPIC_FORMAT = "/topic/chatroom.%s.delete";
  private static final String CHATROOM_READ_TOPIC_FORMAT = "/topic/chatroom.%s.read";
  private static final String USER_STATUS_TOPIC = "/topic/status";

  public void sendChatroomMessage(String chatroomId, ChatMessageResponse message) {
    String destination = String.format(CHATROOM_TOPIC_FORMAT, chatroomId);
    messagingTemplate.convertAndSend(destination, message);
    log.debug("채팅방 메시지 전송 완료 -> 채팅방ID: {}, 메시지ID: {}", chatroomId, message.getId());
  }

  public void sendMessageDeletedEvent(String chatroomId, ChatMessageResponse message) {
    String destination = String.format(CHATROOM_DELETE_TOPIC_FORMAT, chatroomId);
    messagingTemplate.convertAndSend(destination, message);
    log.debug("메시지 삭제 이벤트 전송 완료 -> 채팅방ID: {}, 메시지ID: {}", chatroomId, message.getId());
  }

  public void sendReadStatusEvent(String chatroomId, Object readStatus) {
    String destination = String.format(CHATROOM_READ_TOPIC_FORMAT, chatroomId);
    messagingTemplate.convertAndSend(destination, readStatus);
    log.debug("읽음 상태 이벤트 전송 완료 -> 채팅방ID: {}", chatroomId);
  }

  public void sendUserStatusEvent(Object statusMessage) {
    messagingTemplate.convertAndSend(USER_STATUS_TOPIC, statusMessage);
    log.debug("사용자 상태 변경 이벤트 전송 완료: {}", statusMessage);
  }

  public void sendToUser(String userId, String destination, Object message) {
    messagingTemplate.convertAndSendToUser(userId, destination, message);
    log.debug("사용자 메시지 전송 완료 -> 사용자ID: {}, 경로: {}", userId, destination);
  }
}
