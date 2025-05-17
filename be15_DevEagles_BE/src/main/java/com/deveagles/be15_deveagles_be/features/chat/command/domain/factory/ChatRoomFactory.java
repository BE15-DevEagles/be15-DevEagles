package com.deveagles.be15_deveagles_be.features.chat.command.domain.factory;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import java.time.LocalDateTime;

public class ChatRoomFactory {

  public static ChatRoom createRegularChatRoom(
      String teamId, String name, ChatRoomType type, boolean isDefault) {
    return ChatRoom.builder()
        .teamId(teamId)
        .name(name)
        .type(type)
        .isDefault(isDefault)
        .createdAt(LocalDateTime.now())
        .build();
  }

  public static ChatRoom createDefaultChatRoom(String teamId, String name) {
    return ChatRoom.builder()
        .teamId(teamId)
        .name(name)
        .type(ChatRoomType.TEAM)
        .isDefault(true)
        .createdAt(LocalDateTime.now())
        .build();
  }

  public static ChatRoom createAiChatRoom(String teamId, String userId, String name) {
    return ChatRoom.builder()
        .teamId(teamId)
        .userId(userId)
        .name(name)
        .type(ChatRoomType.AI)
        .isDefault(false)
        .createdAt(LocalDateTime.now())
        .build();
  }
}
