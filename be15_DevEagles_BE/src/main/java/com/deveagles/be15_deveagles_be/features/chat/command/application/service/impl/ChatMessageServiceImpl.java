package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import com.deveagles.be15_deveagles_be.common.dto.PagedResult;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatMessageRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final WebSocketMessageService webSocketMessageService;
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

  public ChatMessageServiceImpl(
      ChatMessageRepository chatMessageRepository,
      ChatRoomRepository chatRoomRepository,
      WebSocketMessageService webSocketMessageService,
      RedisTemplate<String, String> redisTemplate,
      ObjectMapper objectMapper) {
    this.chatMessageRepository = chatMessageRepository;
    this.chatRoomRepository = chatRoomRepository;
    this.webSocketMessageService = webSocketMessageService;
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional
  public ChatMessageResponse sendMessage(ChatMessageRequest request) {
    ChatRoom chatRoom =
        chatRoomRepository
            .findById(request.getChatroomId())
            .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    boolean isParticipant =
        chatRoom.getActiveParticipants().stream()
            .anyMatch(participant -> participant.getUserId().equals(request.getSenderId()));

    if (!isParticipant) {
      throw new ChatBusinessException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
    }

    // UTC로 현재 시간 생성 (시간대 문제 해결)
    LocalDateTime utcTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();

    ChatMessage message =
        ChatMessage.builder()
            .chatroomId(request.getChatroomId())
            .senderId(request.getSenderId())
            .senderName(request.getSenderName())
            .messageType(request.getMessageType())
            .content(request.getContent())
            .metadata(request.getMetadata())
            .createdAt(utcTime)
            .build();

    // MongoDB에 메시지 저장 (Primary Storage)
    ChatMessage savedMessage = chatMessageRepository.save(message);

    // 채팅방 마지막 메시지 정보 업데이트
    updateChatRoomLastMessage(chatRoom, savedMessage);

    ChatMessageResponse response = ChatMessageResponse.from(savedMessage);

    webSocketMessageService.sendChatroomMessage(request.getChatroomId(), response);

    try {
      String messageJson = objectMapper.writeValueAsString(response);
      String redisKey = "chat:messages:" + request.getChatroomId();

      // UTC 기준으로 score 계산 (시간대 문제 해결)
      double score = savedMessage.getCreatedAt().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

      redisTemplate.opsForZSet().add(redisKey, messageJson, score);

      Long size = redisTemplate.opsForZSet().size(redisKey);
      if (size != null && size > 100) {
        redisTemplate.opsForZSet().removeRange(redisKey, 0, 0);
      }
    } catch (Exception e) {
      System.err.println("Failed to cache message in Redis: " + e.getMessage());
    }

    return response;
  }

  @Override
  public Optional<ChatMessageResponse> getMessage(String messageId) {
    return chatMessageRepository.findById(messageId).map(ChatMessageResponse::from);
  }

  @Override
  public List<ChatMessageResponse> getMessagesByChatroom(String chatroomId, int page, int size) {
    chatRoomRepository
        .findById(chatroomId)
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    PagedResult<ChatMessage> messages =
        chatMessageRepository.findMessagesByChatroomIdWithPagination(chatroomId, page, size);

    return messages.getContent().stream()
        .map(ChatMessageResponse::from)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<ChatMessageResponse> deleteMessage(String messageId) {
    Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);

    if (messageOpt.isEmpty()) {
      return Optional.empty();
    }

    ChatMessage message = messageOpt.get();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new ChatBusinessException(ChatErrorCode.MESSAGE_DELETE_ACCESS_DENIED);
    }

    String currentUserId = authentication.getName();

    if (!message.getSenderId().equals(currentUserId)) {
      throw new ChatBusinessException(ChatErrorCode.MESSAGE_DELETE_ACCESS_DENIED);
    }

    message.delete();
    ChatMessage updatedMessage = chatMessageRepository.save(message);

    ChatMessageResponse response = ChatMessageResponse.from(updatedMessage);
    webSocketMessageService.sendMessageDeletedEvent(message.getChatroomId(), response);

    return Optional.of(response);
  }

  @Override
  public List<ChatMessageResponse> getMessagesByChatroomBefore(
      String chatroomId, String messageId, int limit) {
    chatRoomRepository
        .findById(chatroomId)
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);
    if (messageOpt.isEmpty()) {
      throw new ChatBusinessException(ChatErrorCode.MESSAGE_NOT_FOUND);
    }

    ChatMessage message = messageOpt.get();
    List<ChatMessage> messages =
        chatMessageRepository.findMessagesByChatroomIdBeforeTimestamp(
            chatroomId, message.getCreatedAt(), limit);

    return messages.stream().map(ChatMessageResponse::from).collect(Collectors.toList());
  }

  @Override
  public List<ChatMessageResponse> getMessagesByChatroomAfter(
      String chatroomId, String messageId, int limit) {
    chatRoomRepository
        .findById(chatroomId)
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);
    if (messageOpt.isEmpty()) {
      throw new ChatBusinessException(ChatErrorCode.MESSAGE_NOT_FOUND);
    }

    ChatMessage message = messageOpt.get();
    List<ChatMessage> messages =
        chatMessageRepository.findMessagesByChatroomIdAfterTimestamp(
            chatroomId, message.getCreatedAt(), limit);

    return messages.stream().map(ChatMessageResponse::from).collect(Collectors.toList());
  }

  private void updateChatRoomLastMessage(ChatRoom chatRoom, ChatMessage message) {
    ChatRoom.LastMessageInfo lastMessageInfo =
        ChatRoom.LastMessageInfo.builder()
            .id(message.getId())
            .content(message.getContent())
            .senderId(message.getSenderId())
            .senderName(message.getSenderName())
            .sentAt(message.getCreatedAt())
            .build();

    chatRoom.updateLastMessage(lastMessageInfo);
    chatRoomRepository.save(chatRoom);
  }
}
