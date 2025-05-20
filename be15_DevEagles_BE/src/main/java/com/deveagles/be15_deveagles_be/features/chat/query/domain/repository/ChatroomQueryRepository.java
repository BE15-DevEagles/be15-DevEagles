package com.deveagles.be15_deveagles_be.features.chat.query.domain.repository;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import java.util.List;
import java.util.Optional;

public interface ChatroomQueryRepository {

  List<ChatRoom> findChatrooms(String teamId, int page, int size);

  int countChatrooms(String teamId);

  Optional<ChatRoom> findChatroomById(String chatroomId);

  Optional<ChatRoom> findChatroomReadSummaryById(String chatroomId);
}
