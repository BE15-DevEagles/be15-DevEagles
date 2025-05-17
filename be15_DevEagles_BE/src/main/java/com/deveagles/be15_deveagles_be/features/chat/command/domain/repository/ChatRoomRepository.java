package com.deveagles.be15_deveagles_be.features.chat.command.domain.repository;

import com.deveagles.be15_deveagles_be.common.dto.PagedResult;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository {
  ChatRoom save(ChatRoom chatRoom);

  Optional<ChatRoom> findById(String id);

  List<ChatRoom> findByTeamId(String teamId);

  List<ChatRoom> findActiveChatRoomsByTeamId(String teamId);

  PagedResult<ChatRoom> findActiveChatRoomsByTeamIdWithPagination(
      String teamId, int page, int size);

  PagedResult<ChatRoom> findActiveChatRoomsByType(ChatRoomType type, int page, int size);

  Optional<ChatRoom> findDefaultChatRoomByTeamId(String teamId);

  List<ChatRoom> findActiveChatRoomsByTeamIdAndType(String teamId, ChatRoomType type);

  Optional<ChatRoom> findByTeamIdAndUserIdAndTypeAndDeletedAtIsNull(
      String teamId, String userId, ChatRoomType type);

  void delete(ChatRoom chatRoom);

  void deleteById(String id);
}
