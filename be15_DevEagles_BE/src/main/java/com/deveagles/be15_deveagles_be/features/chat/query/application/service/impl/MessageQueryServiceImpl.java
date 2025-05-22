package com.deveagles.be15_deveagles_be.features.chat.query.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageListResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageReadStatusResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.service.MessageQueryService;
import com.deveagles.be15_deveagles_be.features.chat.query.domain.repository.MessageQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageQueryServiceImpl implements MessageQueryService {

  private final MessageQueryRepository messageQueryRepository;
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  private static final String REDIS_CHAT_MESSAGES_KEY_PREFIX = "chat:messages:";
  private static final int DEFAULT_REDIS_LOOKUP_LIMIT = 50;

  @Override
  public MessageListResponse getMessages(Long userId, String chatroomId, String before, int limit) {
    log.info(
        "메시지 조회 서비스 -> 사용자ID: {}, 채팅방ID: {}, 이전메시지ID(커서): {}, 요청개수: {}",
        userId,
        chatroomId,
        before,
        limit);

    List<MessageResponse> messagesFromRedis = new ArrayList<>();

    // Redis 조회는 redisTemplate이 null이 아닐 때만 시도
    if (redisTemplate != null) {
      String redisKey = REDIS_CHAT_MESSAGES_KEY_PREFIX + chatroomId;
      long longBeforeTimestamp = Long.MAX_VALUE;

      try {
        Set<ZSetOperations.TypedTuple<String>> typedTuples =
            redisTemplate
                .opsForZSet()
                .reverseRangeByScoreWithScores(
                    redisKey,
                    Double.NEGATIVE_INFINITY,
                    (double) longBeforeTimestamp - 1,
                    0,
                    limit > 0 ? limit : DEFAULT_REDIS_LOOKUP_LIMIT);

        if (typedTuples != null) {
          for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            if (tuple.getValue() != null) {
              try {
                ChatMessageResponse message =
                    objectMapper.readValue(tuple.getValue(), ChatMessageResponse.class);
                messagesFromRedis.add(convertToMessageResponse(message));
              } catch (Exception e) {
                log.error(
                    "Redis 메시지 역직렬화 중 오류 발생: data={}, error={}", tuple.getValue(), e.getMessage());
              }
            }
          }
        }
      } catch (Exception e) {
        log.error("Redis에서 메시지 조회 중 오류 발생: chatroomId={}, error={}", chatroomId, e.getMessage());
      }
    }
    // Redis가 null인 경우 로그만 남김
    else {
      log.info("Redis 연결이 구성되지 않았습니다. MongoDB에서만 데이터를 조회합니다.");
    }

    List<MessageResponse> finalMessages;

    if (messagesFromRedis.size() < limit || (before != null && !before.isEmpty())) {
      String mongoCursor = before;
      int mongoLimit = limit - messagesFromRedis.size();

      if (mongoLimit <= 0
          && messagesFromRedis.size() >= limit
          && (before == null || before.isEmpty())) {
        finalMessages = messagesFromRedis.stream().limit(limit).collect(Collectors.toList());
      } else {
        int actualMongoLimit =
            (before != null || messagesFromRedis.size() < limit)
                ? (mongoLimit > 0 ? mongoLimit : limit)
                : 0;

        if (actualMongoLimit == 0 && messagesFromRedis.size() >= limit) {
          finalMessages = messagesFromRedis.stream().limit(limit).collect(Collectors.toList());
        } else {
          if (actualMongoLimit == 0 && messagesFromRedis.size() < limit)
            actualMongoLimit = limit - messagesFromRedis.size();
          if (actualMongoLimit < 0) actualMongoLimit = limit;

          List<ChatMessage> messagesFromDb = Collections.emptyList();
          if (actualMongoLimit > 0) {
            messagesFromDb =
                messageQueryRepository.findMessages(chatroomId, mongoCursor, actualMongoLimit);
          }

          List<MessageResponse> messagesFromDbResponse =
              messagesFromDb.stream()
                  .map(this::convertChatMessageToMessageResponse)
                  .collect(Collectors.toList());

          finalMessages =
              Stream.concat(messagesFromRedis.stream(), messagesFromDbResponse.stream())
                  .distinct()
                  .sorted(Comparator.comparing(MessageResponse::getCreatedAt).reversed())
                  .limit(limit)
                  .collect(Collectors.toList());
        }
      }
    } else {
      finalMessages = messagesFromRedis.stream().limit(limit).collect(Collectors.toList());
    }

    return MessageListResponse.of(finalMessages);
  }

  @Override
  public MessageReadStatusResponse getMessageReadStatus(
      Long userId, String chatroomId, String messageId) {
    log.info("메시지 읽음 상태 조회 서비스 -> 사용자ID: {}, 채팅방ID: {}, 메시지ID: {}", userId, chatroomId, messageId);

    Map<String, Object> readStatusData =
        messageQueryRepository.getMessageReadStatus(chatroomId, messageId);

    if (readStatusData == null || readStatusData.isEmpty()) {
      return MessageReadStatusResponse.builder()
          .totalParticipants(0)
          .readCount(0)
          .unreadCount(0)
          .readBy(Collections.emptyList())
          .notReadBy(Collections.emptyList())
          .build();
    }

    List<Map<String, Object>> readUsersRaw =
        (List<Map<String, Object>>) readStatusData.get("readUsers");
    List<Map<String, Object>> unreadUsersRaw =
        (List<Map<String, Object>>) readStatusData.get("unreadUsers");
    Integer totalParticipants = (Integer) readStatusData.get("totalParticipants");

    List<MessageReadStatusResponse.ReadByUserDto> readByList = new ArrayList<>();
    if (readUsersRaw != null) {
      readByList =
          readUsersRaw.stream()
              .map(
                  userMap ->
                      MessageReadStatusResponse.ReadByUserDto.builder()
                          .userId((String) userMap.get("userId"))
                          .userName((String) userMap.get("userName"))
                          .userThumbnailUrl((String) userMap.get("thumbnailUrl"))
                          .readAt((java.time.LocalDateTime) userMap.get("readAt"))
                          .build())
              .collect(Collectors.toList());
    }

    List<MessageReadStatusResponse.NotReadByUserDto> notReadByList = new ArrayList<>();
    if (unreadUsersRaw != null) {
      notReadByList =
          unreadUsersRaw.stream()
              .map(
                  userMap ->
                      MessageReadStatusResponse.NotReadByUserDto.builder()
                          .userId((String) userMap.get("userId"))
                          .userName((String) userMap.get("userName"))
                          .userThumbnailUrl((String) userMap.get("thumbnailUrl"))
                          .build())
              .collect(Collectors.toList());
    }

    return MessageReadStatusResponse.builder()
        .totalParticipants(totalParticipants != null ? totalParticipants : 0)
        .readCount(readByList.size())
        .unreadCount(notReadByList.size())
        .readBy(readByList)
        .notReadBy(notReadByList)
        .build();
  }

  private MessageResponse convertChatMessageToMessageResponse(ChatMessage message) {
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

  private MessageResponse convertToMessageResponse(ChatMessageResponse message) {
    return MessageResponse.builder()
        .id(message.getId())
        .chatroomId(message.getChatroomId())
        .senderId(message.getSenderId())
        .senderName(message.getSenderName())
        .messageType(message.getMessageType().name())
        .content(message.getContent())
        .metadata(message.getMetadata())
        .createdAt(message.getCreatedAt())
        .deletedAt(message.isDeleted() ? message.getCreatedAt() : null)
        .build();
  }
}
