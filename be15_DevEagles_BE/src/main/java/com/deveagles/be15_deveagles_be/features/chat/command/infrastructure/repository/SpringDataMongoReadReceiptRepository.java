package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ReadReceipt;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMongoReadReceiptRepository extends MongoRepository<ReadReceipt, String> {
  List<ReadReceipt> findByMessageId(String messageId);

  Optional<ReadReceipt> findByMessageIdAndUserId(String messageId, String userId);

  List<ReadReceipt> findByUserIdAndMessageIdIn(String userId, Collection<String> messageIds);

  List<ReadReceipt> findByUserIdAndReadAtAfter(String userId, LocalDateTime after);

  long countByMessageId(String messageId);

  List<ReadReceipt> findByChatroomIdAndUserIdAndReadAtAfter(
      String chatroomId, String userId, LocalDateTime after);

  void deleteByMessageId(String messageId);

  List<ReadReceipt> findByChatroomIdAndReadAtBetween(
      String chatroomId, LocalDateTime start, LocalDateTime end);
}
