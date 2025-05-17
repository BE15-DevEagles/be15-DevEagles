package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository;

import com.deveagles.be15_deveagles_be.common.dto.PagedResult;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatMessageRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class MongoChatMessageRepositoryImpl implements ChatMessageRepository {

  private final SpringDataMongoChatMessageRepository repository;

  public MongoChatMessageRepositoryImpl(SpringDataMongoChatMessageRepository repository) {
    this.repository = repository;
  }

  @Override
  public ChatMessage save(ChatMessage message) {
    return repository.save(message);
  }

  @Override
  public Optional<ChatMessage> findById(String id) {
    return repository.findById(id);
  }

  @Override
  public List<ChatMessage> findByChatroomId(String chatroomId) {
    return repository.findByChatroomId(chatroomId);
  }

  @Override
  public List<ChatMessage> findActiveMessagesByChatroomId(String chatroomId) {
    return repository.findByChatroomIdAndDeletedAtIsNull(chatroomId);
  }

  @Override
  public List<ChatMessage> findMessagesByChatroomIdBeforeTimestamp(
      String chatroomId, LocalDateTime timestamp, int limit) {
    Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findByChatroomIdAndCreatedAtBeforeAndDeletedAtIsNull(
        chatroomId, timestamp, pageable);
  }

  @Override
  public List<ChatMessage> findMessagesByChatroomIdAfterTimestamp(
      String chatroomId, LocalDateTime timestamp, int limit) {
    Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "createdAt"));
    return repository.findByChatroomIdAndCreatedAtAfterAndDeletedAtIsNull(
        chatroomId, timestamp, pageable);
  }

  @Override
  public PagedResult<ChatMessage> findMessagesByChatroomIdWithPagination(
      String chatroomId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<ChatMessage> messagePage =
        repository.findByChatroomIdAndDeletedAtIsNull(chatroomId, pageable);

    return PagedResult.from(messagePage);
  }

  @Override
  public long countUnreadMessagesByChatroomIdAfterTimestamp(
      String chatroomId, LocalDateTime timestamp) {
    return repository.countByChatroomIdAndCreatedAtAfterAndDeletedAtIsNull(chatroomId, timestamp);
  }

  @Override
  public void delete(ChatMessage message) {
    repository.delete(message);
  }

  @Override
  public void deleteById(String id) {
    repository.deleteById(id);
  }
}
