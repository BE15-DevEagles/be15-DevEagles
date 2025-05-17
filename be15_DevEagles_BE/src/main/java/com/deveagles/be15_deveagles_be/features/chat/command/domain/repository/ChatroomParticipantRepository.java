package com.deveagles.be15_deveagles_be.features.chat.command.domain.repository;

import com.deveagles.be15_deveagles_be.common.dto.PagedResult;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatroomParticipant;
import java.util.List;
import java.util.Optional;

public interface ChatroomParticipantRepository {
  ChatroomParticipant save(ChatroomParticipant participant);

  Optional<ChatroomParticipant> findById(String id);

  Optional<ChatroomParticipant> findByChatroomIdAndUserId(String chatroomId, String userId);

  List<ChatroomParticipant> findByChatroomId(String chatroomId);

  List<ChatroomParticipant> findActiveByChatroomId(String chatroomId);

  List<ChatroomParticipant> findByUserIdAndDeletedAtIsNull(String userId);

  PagedResult<ChatroomParticipant> findByUserIdAndDeletedAtIsNull(
      String userId, int page, int size);

  void delete(ChatroomParticipant participant);

  void deleteById(String id);

  long countByChatroomIdAndDeletedAtIsNull(String chatroomId);
}
