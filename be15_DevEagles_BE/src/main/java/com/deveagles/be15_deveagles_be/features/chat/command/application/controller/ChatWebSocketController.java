package com.deveagles.be15_deveagles_be.features.chat.command.application.controller;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.AiChatService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl.WebSocketMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import java.security.Principal;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

  private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

  private final ChatMessageService chatMessageService;
  private final ChatRoomService chatRoomService;
  private final AiChatService aiChatService;
  private final ChatRoomRepository chatRoomRepository;
  private final WebSocketMessageService webSocketMessageService;
  private final RedisTemplate<String, String> redisTemplate;

  public ChatWebSocketController(
      ChatMessageService chatMessageService,
      ChatRoomService chatRoomService,
      AiChatService aiChatService,
      ChatRoomRepository chatRoomRepository,
      WebSocketMessageService webSocketMessageService,
      RedisTemplate<String, String> redisTemplate) {
    this.chatMessageService = chatMessageService;
    this.chatRoomService = chatRoomService;
    this.aiChatService = aiChatService;
    this.chatRoomRepository = chatRoomRepository;
    this.webSocketMessageService = webSocketMessageService;
    this.redisTemplate = redisTemplate;
  }

  @MessageMapping("/chat.send")
  public ChatMessageResponse sendMessage(
      @Payload ChatMessageRequest request,
      SimpMessageHeaderAccessor headerAccessor,
      Principal principal) {
    ChatMessageRequest finalRequest;
    if (principal != null && !principal.getName().equals(request.getSenderId())) {
      finalRequest =
          ChatMessageRequest.builder()
              .chatroomId(request.getChatroomId())
              .senderId(principal.getName())
              .senderName(request.getSenderName())
              .messageType(request.getMessageType())
              .content(request.getContent())
              .metadata(request.getMetadata())
              .build();
    } else {
      finalRequest = request;
    }

    ChatMessageResponse userMessageResponse = chatMessageService.sendMessage(finalRequest);

    final ChatMessageRequest aiRequest = finalRequest; // 람다에서 사용할 final 변수
    chatRoomRepository
        .findById(finalRequest.getChatroomId())
        .ifPresent(
            chatRoom -> {
              if (chatRoom.getType() == ChatRoomType.AI) {
                log.info("AI 채팅방에 메시지 수신: {}", aiRequest.getContent());

                CompletableFuture.runAsync(
                    () -> {
                      try {
                        aiChatService.processUserMessage(aiRequest);
                      } catch (Exception e) {
                        log.error("AI 응답 생성 중 오류 발생", e);
                      }
                    });
              }
            });

    return userMessageResponse;
  }

  @MessageMapping("/chat.read")
  public void markAsRead(@Payload ReadMessageRequest request, Principal principal) {
    if (principal == null || request.getChatroomId() == null || request.getMessageId() == null) {
      log.warn("Invalid markAsRead request: principal or chatroomId or messageId is null");
      return;
    }

    String userId = principal.getName();
    String chatroomId = request.getChatroomId();
    String messageId = request.getMessageId();

    try {
      String redisKey = "chat:last_read_message:" + chatroomId;
      redisTemplate.opsForHash().put(redisKey, userId, messageId);
      log.info(
          "User {} marked message {} as read in chatroom {} (Redis)",
          userId,
          messageId,
          chatroomId);
    } catch (Exception e) {
      log.error(
          "Failed to update read status in Redis for user {} in chatroom {}: {}",
          userId,
          chatroomId,
          e.getMessage(),
          e);
      // Redis 실패 시에도 기존 로직은 계속 진행되도록 할 수 있음
    }

    // 기존 로직 호출 (MongoDB 업데이트 및 이벤트 전송 등)
    // ChatRoomResponse chatRoom = chatRoomService.updateLastReadMessage(chatroomId, userId,
    // messageId);
    // 이 호출의 원래 목적과 Redis 도입 후의 역할을 명확히 하여 중복/누락 없도록 조정 필요.
    // 예를 들어, 이 서비스가 ReadReceipt 엔티티를 MongoDB에 저장하고 있었다면,
    // 해당 로직은 유지하되, Redis 업데이트 실패 시 fallback으로 동작하거나, 별도의 배치 작업으로 옮기는 것을 고려.
    // 우선은 기존 호출을 그대로 두되, 그 내용을 파악하여 추후 리팩토링.

    // chatRoomService.updateLastReadMessage(chatroomId, userId, messageId); // 임시 주석 처리 또는 역할 재정의
    // 필요

    // 읽음 상태 응답 생성 및 WebSocket 이벤트 전송 (이 부분은 기존 로직을 활용)
    ReadStatusResponse readStatusResponse = new ReadStatusResponse(chatroomId, userId, messageId);

    webSocketMessageService.sendReadStatusEvent(chatroomId, readStatusResponse);
  }

  @MessageMapping("/chat.ai.init")
  public void initializeAiChat(@Payload AiChatInitRequest request, Principal principal) {
    if (principal == null) {
      return;
    }

    String userId = principal.getName();
    aiChatService.initializeAiChatSession(userId, request.getChatroomId());

    log.info("AI 채팅 세션 초기화 요청: 사용자={}, 채팅방={}", userId, request.getChatroomId());
  }

  public static class ReadMessageRequest {
    private String chatroomId;
    private String messageId;

    public String getChatroomId() {
      return chatroomId;
    }

    public String getMessageId() {
      return messageId;
    }
  }

  public static class ReadStatusResponse {
    private String chatroomId;
    private String userId;
    private String lastReadMessageId;

    public ReadStatusResponse(String chatroomId, String userId, String lastReadMessageId) {
      this.chatroomId = chatroomId;
      this.userId = userId;
      this.lastReadMessageId = lastReadMessageId;
    }

    public String getChatroomId() {
      return chatroomId;
    }

    public String getUserId() {
      return userId;
    }

    public String getLastReadMessageId() {
      return lastReadMessageId;
    }
  }

  public static class AiChatInitRequest {
    private String chatroomId;

    public String getChatroomId() {
      return chatroomId;
    }
  }
}
