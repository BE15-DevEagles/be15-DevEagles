package com.deveagles.be15_deveagles_be.features.chat.command.application.service.util;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ChatRoomValidator {

  private final ChatRoomRepository chatRoomRepository;

  public ChatRoomValidator(ChatRoomRepository chatRoomRepository) {
    this.chatRoomRepository = chatRoomRepository;
  }

  public void validateDefaultChatRoomNotExists(String teamId) {
    Optional<ChatRoom> existingDefaultChatRoom =
        chatRoomRepository.findDefaultChatRoomByTeamId(teamId);

    if (existingDefaultChatRoom.isPresent()) {
      throw new ChatBusinessException(ChatErrorCode.DEFAULT_CHATROOM_ALREADY_EXISTS);
    }
  }

  public ChatRoom validateAndGetChatRoom(String chatroomId) {
    return chatRoomRepository
        .findById(chatroomId)
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
  }

  public void validateNotDefaultChatRoom(ChatRoom chatRoom) {
    if (chatRoom.isDefault()) {
      throw new ChatBusinessException(ChatErrorCode.DEFAULT_CHATROOM_CANNOT_BE_DELETED);
    }
  }
}
