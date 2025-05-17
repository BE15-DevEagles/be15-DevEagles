package com.deveagles.be15_deveagles_be.features.chat.command.domain.repository;

import com.deveagles.be15_deveagles_be.common.dto.Pagination;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository {
  ChatRoom save(ChatRoom chatRoom);

  Optional<ChatRoom> findById(String id);

  List<ChatRoom> findByTeamId(String teamId);

  List<ChatRoom> findActiveChatRoomsByTeamId(String teamId);

  List<ChatRoom> findActiveChatRoomsByTeamIdWithPagination(
      String teamId, int page, int size, Pagination pagination);

  List<ChatRoom> findActiveChatRoomsByType(
      ChatRoomType type, int page, int size, Pagination pagination);

  Optional<ChatRoom> findDefaultChatRoomByTeamId(String teamId);

  List<ChatRoom> findActiveChatRoomsByTeamIdAndType(String teamId, ChatRoomType type);

  void delete(ChatRoom chatRoom);

  void deleteById(String id);
}
