package com.deveagles.be15_deveagles_be.features.chat.query.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageListResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageReadStatusResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.service.MessageQueryService;
import com.deveagles.be15_deveagles_be.features.chat.query.domain.repository.MessageQueryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageQueryServiceImpl implements MessageQueryService {

  private final MessageQueryRepository messageQueryRepository;

  @Override
  public MessageListResponse getMessages(Long userId, String chatroomId, String before, int limit) {
    log.info(
        "메시지 조회 서비스 -> 사용자ID: {}, 채팅방ID: {}, 이전메시지ID: {}, 제한: {}",
        userId,
        chatroomId,
        before,
        limit);

    List<ChatMessage> messages = messageQueryRepository.findMessages(chatroomId, before, limit);

    List<MessageResponse> messageResponses =
        messages.stream().map(this::convertToMessageResponse).collect(Collectors.toList());

    return MessageListResponse.of(messageResponses);
  }

  @Override
  public MessageReadStatusResponse getMessageReadStatus(
      Long userId, String chatroomId, String messageId) {
    log.info("메시지 읽음 상태 조회 서비스 -> 사용자ID: {}, 채팅방ID: {}, 메시지ID: {}", userId, chatroomId, messageId);

    Map<String, Object> readStatusData =
        messageQueryRepository.getMessageReadStatus(chatroomId, messageId);

    if (readStatusData == null || readStatusData.isEmpty()) {
      return null;
    }

    List<Map<String, Object>> readUsers =
        (List<Map<String, Object>>) readStatusData.get("readUsers");

    List<Map<String, Object>> unreadUsers =
        (List<Map<String, Object>>) readStatusData.get("unreadUsers");

    Integer totalParticipants = (Integer) readStatusData.get("totalParticipants");

    List<MessageReadStatusResponse.ReadByUserDto> readByList = new ArrayList<>();
    if (readUsers != null) {
      readByList =
          readUsers.stream()
              .map(
                  user ->
                      MessageReadStatusResponse.ReadByUserDto.builder()
                          .userId((String) user.get("userId"))
                          .userName((String) user.get("userName"))
                          .userThumbnailUrl((String) user.get("thumbnailUrl"))
                          .readAt((java.time.LocalDateTime) user.get("readAt"))
                          .build())
              .collect(Collectors.toList());
    }

    List<MessageReadStatusResponse.NotReadByUserDto> notReadByList = new ArrayList<>();
    if (unreadUsers != null) {
      notReadByList =
          unreadUsers.stream()
              .map(
                  user ->
                      MessageReadStatusResponse.NotReadByUserDto.builder()
                          .userId((String) user.get("userId"))
                          .userName((String) user.get("userName"))
                          .userThumbnailUrl((String) user.get("thumbnailUrl"))
                          .build())
              .collect(Collectors.toList());
    }

    return MessageReadStatusResponse.builder()
        .totalParticipants(totalParticipants)
        .readCount(readByList.size())
        .unreadCount(notReadByList.size())
        .readBy(readByList)
        .notReadBy(notReadByList)
        .build();
  }

  private MessageResponse convertToMessageResponse(ChatMessage message) {
    return MessageResponse.builder()
        .id(message.getId())
        .chatroomId(message.getChatroomId())
        .senderId(message.getSenderId())
        .senderName(message.getSenderName())
        .messageType(message.getMessageType().name())
        .content(message.getContent())
        .metadata(message.getMetadata())
        .createdAt(message.getCreatedAt())
        .deletedAt(message.getDeletedAt())
        .build();
  }
}
