package com.deveagles.be15_deveagles_be.features.chat.infrastructure.adapters;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatMessageRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatInitializer {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final MongoTemplate mongoTemplate;
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  private static final String REDIS_READ_MESSAGES_PREFIX = "chat:last_read_message:";
  private static final String REDIS_CHAT_MESSAGES_PREFIX = "chat:messages:";
  private static final int MAX_INITIAL_MESSAGES = 50;

  @PostConstruct
  public void initialize() {
    try {
      log.info("채팅 시스템 초기화 시작...");
      initializeRecentMessages();
      initializeReadStatus();
      log.info("채팅 시스템 초기화 완료!");
    } catch (Exception e) {
      log.error("채팅 시스템 초기화 중 오류 발생: {}", e.getMessage(), e);
    }
  }

  private void initializeRecentMessages() {
    log.info("최근 채팅 메시지 Redis 캐싱 시작...");
    int roomCount = 0;
    int messageCount = 0;

    Query chatRoomQuery = new Query(Criteria.where("deletedAt").isNull());
    List<ChatRoom> chatRooms = mongoTemplate.find(chatRoomQuery, ChatRoom.class);

    for (ChatRoom chatRoom : chatRooms) {
      String chatroomId = chatRoom.getId();
      String redisKey = REDIS_CHAT_MESSAGES_PREFIX + chatroomId;

      Long existingCount = redisTemplate.opsForZSet().size(redisKey);
      if (existingCount != null && existingCount > 0) {
        log.debug("채팅방 {}의 메시지 {}개가 이미 Redis에 캐싱되어 있습니다. 건너뜁니다.", chatroomId, existingCount);
        continue;
      }

      Query query = new Query();
      query.addCriteria(Criteria.where("chatroomId").is(chatroomId).and("deletedAt").isNull());
      query.with(Sort.by(Direction.DESC, "createdAt"));
      query.limit(MAX_INITIAL_MESSAGES);

      List<ChatMessage> recentMessages = mongoTemplate.find(query, ChatMessage.class);
      if (!recentMessages.isEmpty()) {
        for (ChatMessage message : recentMessages) {
          try {
            ChatMessageResponse response = ChatMessageResponse.from(message);
            String messageJson = objectMapper.writeValueAsString(response);
            double score = message.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli();
            redisTemplate.opsForZSet().add(redisKey, messageJson, score);
            messageCount++;
          } catch (Exception e) {
            log.error("메시지 캐싱 중 오류: messageId={}, error={}", message.getId(), e.getMessage());
          }
        }
        roomCount++;
        log.debug("채팅방 {}의 최근 메시지 {}개를 Redis에 캐싱했습니다.", chatroomId, recentMessages.size());
      }
    }
    log.info("{}개 채팅방의 총 {}개 메시지를 Redis에 캐싱 완료", roomCount, messageCount);
  }

  private void initializeReadStatus() {
    log.info("읽음 상태 Redis 동기화 시작...");
    int participantCount = 0;

    Query chatRoomQuery = new Query(Criteria.where("deletedAt").isNull());
    List<ChatRoom> chatRooms = mongoTemplate.find(chatRoomQuery, ChatRoom.class);

    for (ChatRoom chatRoom : chatRooms) {
      String chatroomId = chatRoom.getId();
      String redisKey = REDIS_READ_MESSAGES_PREFIX + chatroomId;

      for (ChatRoom.Participant participant : chatRoom.getActiveParticipants()) {
        String userId = participant.getUserId();
        String lastReadMessageId = participant.getLastReadMessageId();

        if (lastReadMessageId != null && !lastReadMessageId.isEmpty()) {
          try {
            Object existingData = redisTemplate.opsForHash().get(redisKey, userId);
            if (existingData == null) {
              redisTemplate.opsForHash().put(redisKey, userId, lastReadMessageId);
              participantCount++;
            }
          } catch (Exception e) {
            log.error(
                "참가자 읽음 상태 동기화 중 오류: chatroomId={}, userId={}, error={}",
                chatroomId,
                userId,
                e.getMessage());
          }
        }
      }
    }
    log.info("총 {}개의 참가자 읽음 상태 Redis 동기화 완료", participantCount);
  }
}
