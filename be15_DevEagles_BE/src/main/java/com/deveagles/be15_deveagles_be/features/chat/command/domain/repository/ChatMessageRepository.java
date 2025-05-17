package com.deveagles.be15_deveagles_be.features.chat.command.domain.repository;

import com.deveagles.be15_deveagles_be.common.dto.Pagination;
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

  List<ChatMessage> findMessagesByChatroomIdWithPagination(
      String chatroomId, int page, int size, Pagination pagination);

  long countUnreadMessagesByChatroomIdAfterTimestamp(String chatroomId, LocalDateTime timestamp);

  void delete(ChatMessage message);

  void deleteById(String id);
}
