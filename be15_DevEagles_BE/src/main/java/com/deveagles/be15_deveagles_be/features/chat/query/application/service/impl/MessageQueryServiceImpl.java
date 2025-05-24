package com.deveagles.be15_deveagles_be.features.chat.query.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageListResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageReadStatusResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.service.MessageQueryService;
import com.deveagles.be15_deveagles_be.features.chat.query.application.service.util.MessageResponseConverter;
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
  private final MessageResponseConverter messageResponseConverter;

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
    boolean redisSuccess = false;

    if (redisTemplate != null) {
      String redisKey = REDIS_CHAT_MESSAGES_KEY_PREFIX + chatroomId;
      long longBeforeTimestamp = Long.MAX_VALUE;

      try {
        // before 파라미터가 있으면 해당 메시지의 타임스탬프를 기준으로 조회
        if (before != null && !before.isEmpty()) {
          try {
            ChatMessage beforeMessage =
                messageQueryRepository.findMessages(chatroomId, before, 1).stream()
                    .findFirst()
                    .orElse(null);
            if (beforeMessage != null) {
              longBeforeTimestamp =
                  beforeMessage
                      .getCreatedAt()
                      .atZone(java.time.ZoneId.systemDefault())
                      .toInstant()
                      .toEpochMilli();
            }
          } catch (Exception e) {
            log.warn("이전 메시지 ID로 메시지를 찾을 수 없습니다: {}", before);
          }
        }

        // Redis에서 시간 역순으로 메시지 조회
        Set<ZSetOperations.TypedTuple<String>> typedTuples =
            redisTemplate
                .opsForZSet()
                .reverseRangeByScoreWithScores(
                    redisKey,
                    Double.NEGATIVE_INFINITY,
                    (double) longBeforeTimestamp - 1,
                    0,
                    limit > 0 ? limit : DEFAULT_REDIS_LOOKUP_LIMIT);

        if (typedTuples != null && !typedTuples.isEmpty()) {
          for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            if (tuple.getValue() != null) {
              try {
                ChatMessageResponse message =
                    objectMapper.readValue(tuple.getValue(), ChatMessageResponse.class);
                messagesFromRedis.add(
                    messageResponseConverter.convertFromChatMessageResponse(message));
              } catch (Exception e) {
                log.error(
                    "Redis 메시지 역직렬화 중 오류 발생: data={}, error={}", tuple.getValue(), e.getMessage());
              }
            }
          }
          redisSuccess = !messagesFromRedis.isEmpty();
          log.debug("Redis에서 {}개의 메시지를 조회했습니다.", messagesFromRedis.size());
        }
      } catch (Exception e) {
        log.error("Redis에서 메시지 조회 중 오류 발생: chatroomId={}, error={}", chatroomId, e.getMessage());
      }
    } else {
      log.info("Redis 연결이 구성되지 않았습니다. MongoDB에서만 데이터를 조회합니다.");
    }

    List<MessageResponse> finalMessages;

    // Redis 조회 결과로 충분하면 그대로 반환, 아니면 MongoDB 조회 결과와 병합
    if (redisSuccess && messagesFromRedis.size() >= limit && (before == null || before.isEmpty())) {
      finalMessages = messagesFromRedis.stream().limit(limit).collect(Collectors.toList());
      log.debug("Redis 캐시에서 충분한 데이터를 찾아 반환합니다.");
    } else {
      // Redis에서 충분한 데이터를 가져오지 못했거나 특정 시점(before) 이전 메시지를 요청한 경우
      // MongoDB에서 추가 조회
      int mongoLimit = limit - messagesFromRedis.size();
      if (mongoLimit <= 0) mongoLimit = limit;

      log.debug("MongoDB에서 추가로 {}개의 메시지를 조회합니다.", mongoLimit);
      List<ChatMessage> messagesFromDb =
          messageQueryRepository.findMessages(chatroomId, before, mongoLimit);
      List<MessageResponse> messagesFromDbResponse =
          messagesFromDb.stream()
              .map(messageResponseConverter::convertFromChatMessage)
              .collect(Collectors.toList());

      // Redis와 MongoDB 결과 병합
      finalMessages =
          Stream.concat(messagesFromRedis.stream(), messagesFromDbResponse.stream())
              .distinct()
              .sorted(Comparator.comparing(MessageResponse::getCreatedAt).reversed())
              .limit(limit)
              .collect(Collectors.toList());

      log.debug("최종적으로 Redis와 MongoDB에서 총 {}개의 메시지를 병합하여 반환합니다.", finalMessages.size());
    }

    // 메시지 읽음 상태 표시를 위해 사용자가 지금 메시지를 읽었다는 것을 Redis에 기록
    if (!finalMessages.isEmpty() && userId != null) {
      try {
        String lastMessageId = finalMessages.get(0).getId();
        String redisKey = "chat:last_read_message:" + chatroomId;
        redisTemplate.opsForHash().put(redisKey, userId.toString(), lastMessageId);
        log.debug(
            "사용자 {}의 마지막 읽은 메시지를 Redis에 업데이트: chatroomId={}, messageId={}",
            userId,
            chatroomId,
            lastMessageId);
      } catch (Exception e) {
        log.error("메시지 읽음 상태 Redis 업데이트 실패: {}", e.getMessage());
      }
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
}
