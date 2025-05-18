package com.deveagles.be15_deveagles_be.features.chat.command.domain.repository;

import com.deveagles.be15_deveagles_be.common.dto.PagedResult;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository {
  ChatMessage save(ChatMessage message);

  Optional<ChatMessage> findById(String id);

  List<ChatMessage> findByChatroomId(String chatroomId);

  List<ChatMessage> findActiveMessagesByChatroomId(String chatroomId);

  List<ChatMessage> findMessagesByChatroomIdBeforeTimestamp(
      String chatroomId, LocalDateTime timestamp, int limit);

  List<ChatMessage> findMessagesByChatroomIdAfterTimestamp(
      String chatroomId, LocalDateTime timestamp, int limit);

  PagedResult<ChatMessage> findMessagesByChatroomIdWithPagination(
      String chatroomId, int page, int size);

  long countUnreadMessagesByChatroomIdAfterTimestamp(String chatroomId, LocalDateTime timestamp);

  void delete(ChatMessage message);

  void deleteById(String id);

  List<ChatMessage> findRecentMessagesByChatroomId(String chatroomId, int limit);
}
