package com.deveagles.be15_deveagles_be.features.chat.command.application.controller;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.AiChatService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ReadReceipt;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ReadReceiptRepository;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

  private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);
  private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
  private static final String CHATROOM_READ_TOPIC_FORMAT = "/topic/chatroom.%s.read";

  private final ChatMessageService chatMessageService;
  private final ChatRoomService chatRoomService;
  private final AiChatService aiChatService;
  private final ChatRoomRepository chatRoomRepository;
  private final ReadReceiptRepository readReceiptRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final RedisTemplate<String, String> redisTemplate;

  public ChatWebSocketController(
      ChatMessageService chatMessageService,
      ChatRoomService chatRoomService,
      AiChatService aiChatService,
      ChatRoomRepository chatRoomRepository,
      ReadReceiptRepository readReceiptRepository,
      SimpMessagingTemplate messagingTemplate,
      RedisTemplate<String, String> redisTemplate) {
    this.chatMessageService = chatMessageService;
    this.chatRoomService = chatRoomService;
    this.aiChatService = aiChatService;
    this.chatRoomRepository = chatRoomRepository;
    this.readReceiptRepository = readReceiptRepository;
    this.messagingTemplate = messagingTemplate;
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

    final ChatMessageRequest aiRequest = finalRequest;
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

    boolean redisSuccess = false;
    boolean mongoSuccess = false;

    // 1. Redis에 읽음 상태 저장 (빠른 응답용)
    try {
      String redisKey = "chat:last_read_message:" + chatroomId;
      redisTemplate.opsForHash().put(redisKey, userId, messageId);
      log.info(
          "User {} marked message {} as read in chatroom {} (Redis)",
          userId,
          messageId,
          chatroomId);
      redisSuccess = true;
    } catch (Exception e) {
      log.error(
          "Failed to update read status in Redis for user {} in chatroom {}: {}",
          userId,
          chatroomId,
          e.getMessage(),
          e);
    }

    // 2. ReadReceipt MongoDB에 저장 (상세 이력용)
    try {
      // UTC로 현재 시간 생성 (시간대 문제 해결)
      LocalDateTime utcTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();

      ReadReceipt readReceipt =
          ReadReceipt.builder()
              .messageId(messageId)
              .userId(userId)
              .chatroomId(chatroomId)
              .readAt(utcTime)
              .build();

      // 중복 체크 후 저장
      Optional<ReadReceipt> existing =
          readReceiptRepository.findByMessageIdAndUserId(messageId, userId);
      if (existing.isEmpty()) {
        readReceiptRepository.save(readReceipt);
        log.info(
            "ReadReceipt saved: userId={}, messageId={}, chatroomId={}",
            userId,
            messageId,
            chatroomId);
      } else {
        log.debug("ReadReceipt already exists: userId={}, messageId={}", userId, messageId);
      }
      mongoSuccess = true;
    } catch (Exception e) {
      log.error(
          "Failed to save ReadReceipt: userId={}, messageId={}, error={}",
          userId,
          messageId,
          e.getMessage(),
          e);
    }

    // 3. ChatRoom의 participant lastReadMessage 업데이트 (폴백)
    if (!redisSuccess && !mongoSuccess) {
      try {
        chatRoomService.updateLastReadMessage(chatroomId, userId, messageId);
        log.info(
            "User {} marked message {} as read in chatroom {} (ChatRoom fallback)",
            userId,
            messageId,
            chatroomId);
      } catch (Exception e) {
        log.error(
            "Failed to update read status in ChatRoom for user {} in chatroom {}: {}",
            userId,
            chatroomId,
            e.getMessage(),
            e);
        return;
      }
    }

    // 4. 읽음 상태 이벤트 전송
    ReadStatusResponse readStatusResponse = new ReadStatusResponse(chatroomId, userId, messageId);
    String destination = String.format(CHATROOM_READ_TOPIC_FORMAT, chatroomId);
    messagingTemplate.convertAndSend(destination, readStatusResponse);
    log.debug("읽음 상태 이벤트 전송 완료 -> 채팅방ID: {}", chatroomId);
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
