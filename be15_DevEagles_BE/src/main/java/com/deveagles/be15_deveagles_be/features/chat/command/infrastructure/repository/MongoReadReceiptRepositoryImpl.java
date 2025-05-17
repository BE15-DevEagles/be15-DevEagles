package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ReadReceipt;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ReadReceiptRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MongoReadReceiptRepositoryImpl implements ReadReceiptRepository {

  private final SpringDataMongoReadReceiptRepository repository;

  public MongoReadReceiptRepositoryImpl(SpringDataMongoReadReceiptRepository repository) {
    this.repository = repository;
  }

  @Override
  public ReadReceipt save(ReadReceipt readReceipt) {
    return repository.save(readReceipt);
  }

  @Override
  public List<ReadReceipt> findByMessageId(String messageId) {
    return repository.findByMessageId(messageId);
  }

  @Override
  public Optional<ReadReceipt> findByMessageIdAndUserId(String messageId, String userId) {
    return repository.findByMessageIdAndUserId(messageId, userId);
  }

  @Override
  public List<ReadReceipt> findByUserIdAndMessageIdIn(
      String userId, Collection<String> messageIds) {
    return repository.findByUserIdAndMessageIdIn(userId, messageIds);
  }

  @Override
  public List<ReadReceipt> findByUserIdAndReadAtAfter(String userId, LocalDateTime after) {
    return repository.findByUserIdAndReadAtAfter(userId, after);
  }

  @Override
  public long countByMessageId(String messageId) {
    return repository.countByMessageId(messageId);
  }

  @Override
  public List<ReadReceipt> findByChatroomIdAndUserIdAndReadAtAfter(
      String chatroomId, String userId, LocalDateTime after) {
    return repository.findByChatroomIdAndUserIdAndReadAtAfter(chatroomId, userId, after);
  }

  @Override
  public void deleteByMessageId(String messageId) {
    repository.deleteByMessageId(messageId);
  }
}
