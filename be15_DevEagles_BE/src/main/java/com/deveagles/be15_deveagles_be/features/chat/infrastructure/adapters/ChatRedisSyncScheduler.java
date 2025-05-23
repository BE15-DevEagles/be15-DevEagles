package com.deveagles.be15_deveagles_be.features.chat.infrastructure.adapters;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRedisSyncScheduler {

  private final ChatRoomRepository chatRoomRepository;
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  private static final String REDIS_READ_MESSAGES_PREFIX = "chat:last_read_message:";
  private static final String REDIS_ONLINE_USERS_KEY = "chat:online_users";
  private static final String REDIS_CHAT_MESSAGES_PREFIX = "chat:messages:";

  @Scheduled(fixedRate = 300000)
  @Transactional
  public void syncReadStatusToMongoDB() {
    log.info("Redis에서 MongoDB로 읽음 상태 동기화 시작");

    try {
      Set<String> chatRoomKeys = redisTemplate.keys(REDIS_READ_MESSAGES_PREFIX + "*");

      if (chatRoomKeys == null || chatRoomKeys.isEmpty()) {
        log.info("동기화할 읽음 상태가 없습니다.");
        return;
      }

      int syncCount = 0;

      for (String redisKey : chatRoomKeys) {
        String chatroomId = redisKey.substring(REDIS_READ_MESSAGES_PREFIX.length());

        Map<Object, Object> userReadData = redisTemplate.opsForHash().entries(redisKey);

        if (userReadData.isEmpty()) {
          continue;
        }

        for (Map.Entry<Object, Object> entry : userReadData.entrySet()) {
          String userId = entry.getKey().toString();
          String messageId = entry.getValue().toString();

          try {
            updateChatRoomReadStatus(chatroomId, userId, messageId);
            syncCount++;
          } catch (Exception e) {
            log.error(
                "MongoDB에 읽음 상태 동기화 중 오류: chatroomId={}, userId={}, messageId={}, error={}",
                chatroomId,
                userId,
                messageId,
                e.getMessage());
          }
        }
      }

      log.info("읽음 상태 동기화 완료: {} 개의 항목 처리됨", syncCount);
    } catch (Exception e) {
      log.error("읽음 상태 동기화 중 오류 발생: {}", e.getMessage(), e);
    }
  }

  private void updateChatRoomReadStatus(String chatroomId, String userId, String messageId) {
    chatRoomRepository
        .findById(chatroomId)
        .ifPresent(
            chatRoom -> {
              ChatRoom.Participant participant = chatRoom.getParticipant(userId);

              if (participant != null) {
                participant.updateLastReadMessage(messageId);
                chatRoomRepository.save(chatRoom);
                log.debug(
                    "MongoDB 읽음 상태 업데이트: chatroomId={}, userId={}, messageId={}",
                    chatroomId,
                    userId,
                    messageId);
              }
            });
  }

  @Scheduled(cron = "0 0 * * * *")
  public void cleanupOldRedisData() {
    log.info("오래된 Redis 메시지 데이터 정리 시작");

    try {
      Set<String> messageKeys = redisTemplate.keys(REDIS_CHAT_MESSAGES_PREFIX + "*");

      if (messageKeys == null || messageKeys.isEmpty()) {
        return;
      }

      for (String key : messageKeys) {
        long size = redisTemplate.opsForZSet().size(key);
        if (size > 100) {
          redisTemplate.opsForZSet().removeRange(key, 0, size - 101);
          log.debug("Redis 채팅방 메시지 정리: key={}, 삭제된 메시지 수={}", key, size - 100);
        }
      }

      log.info("Redis 메시지 데이터 정리 완료");
    } catch (Exception e) {
      log.error("Redis 데이터 정리 중 오류 발생: {}", e.getMessage(), e);
    }
  }
}
