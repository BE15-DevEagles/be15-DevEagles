package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMongoChatMessageRepository extends MongoRepository<ChatMessage, String> {
  List<ChatMessage> findByChatroomId(String chatroomId);

  List<ChatMessage> findByChatroomIdAndDeletedAtIsNull(String chatroomId);

  List<ChatMessage> findByChatroomIdAndCreatedAtBeforeAndDeletedAtIsNull(
      String chatroomId, LocalDateTime before, Pageable pageable);

  List<ChatMessage> findByChatroomIdAndCreatedAtAfterAndDeletedAtIsNull(
      String chatroomId, LocalDateTime after, Pageable pageable);

  Page<ChatMessage> findByChatroomIdAndDeletedAtIsNull(String chatroomId, Pageable pageable);

  long countByChatroomIdAndCreatedAtAfterAndDeletedAtIsNull(
      String chatroomId, LocalDateTime timestamp);

  List<ChatMessage> findByChatroomIdAndDeletedAtIsNullOrderByCreatedAtDesc(
      String chatroomId, Pageable pageable);
}
