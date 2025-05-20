package com.deveagles.be15_deveagles_be.features.chat.query.infrastructure.repository;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ReadReceipt;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository.SpringDataMongoChatMessageRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository.SpringDataMongoReadReceiptRepository;
import com.deveagles.be15_deveagles_be.features.chat.query.domain.repository.MessageQueryRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoMessageQueryRepositoryImpl implements MessageQueryRepository {

  private final MongoTemplate mongoTemplate;
  private final SpringDataMongoChatMessageRepository chatMessageRepository;
  private final SpringDataMongoReadReceiptRepository readReceiptRepository;

  @Override
  public List<ChatMessage> findMessages(String chatroomId, String beforeMessageId, int limit) {
    if (chatroomId == null || chatroomId.isBlank()) {
      return new ArrayList<>();
    }

    if (beforeMessageId == null || beforeMessageId.isBlank()) {
      PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Direction.DESC, "createdAt"));
      return chatMessageRepository.findByChatroomIdAndDeletedAtIsNullOrderByCreatedAtDesc(
          chatroomId, pageRequest);
    } else {
      Optional<ChatMessage> optionalMessage = chatMessageRepository.findById(beforeMessageId);
      if (optionalMessage.isEmpty()) {
        return new ArrayList<>();
      }

      ChatMessage message = optionalMessage.get();
      LocalDateTime beforeTime = message.getCreatedAt();

      Query query = new Query();
      query.addCriteria(
          Criteria.where("chatroomId")
              .is(chatroomId)
              .and("createdAt")
              .lt(beforeTime)
              .and("deletedAt")
              .isNull());
      query.with(Sort.by(Direction.DESC, "createdAt"));
      query.limit(limit);

      return mongoTemplate.find(query, ChatMessage.class);
    }
  }

  @Override
  public Map<String, Object> getMessageReadStatus(String chatroomId, String messageId) {
    if (chatroomId == null || chatroomId.isBlank() || messageId == null || messageId.isBlank()) {
      return new HashMap<>();
    }

    Map<String, Object> result = new HashMap<>();

    List<ReadReceipt> readReceipts = readReceiptRepository.findByMessageId(messageId);

    Query chatroomQuery = new Query(Criteria.where("_id").is(chatroomId).and("deletedAt").isNull());
    Map chatroomInfo = mongoTemplate.findOne(chatroomQuery, Map.class, "chatroom");

    if (chatroomInfo == null) {
      return result;
    }

    List<Map<String, Object>> participants =
        (List<Map<String, Object>>) chatroomInfo.get("participants");

    if (participants == null || participants.isEmpty()) {
      return result;
    }

    List<String> readUserIds = readReceipts.stream().map(ReadReceipt::getUserId).toList();

    long activeParticipantsCount =
        participants.stream().filter(p -> p.get("deletedAt") == null).count();

    result.put("totalParticipants", (int) activeParticipantsCount);
    result.put("readCount", readReceipts.size());
    result.put("unreadCount", (int) activeParticipantsCount - readReceipts.size());
    result.put("readReceipts", readReceipts);
    result.put("participants", participants);

    return result;
  }
}
