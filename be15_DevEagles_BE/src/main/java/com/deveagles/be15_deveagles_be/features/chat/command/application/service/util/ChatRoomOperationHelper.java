package com.deveagles.be15_deveagles_be.features.chat.command.application.service.util;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ChatRoomOperationHelper {

  public void executeWithExceptionHandling(
      Runnable operation, ChatErrorCode errorCode, String errorMessage) {
    try {
      operation.run();
    } catch (Exception e) {
      throw new ChatBusinessException(errorCode, errorMessage + ": " + e.getMessage());
    }
  }

  public void validateChatRoomState(ChatRoom chatRoom) {
    if (chatRoom.isDeleted()) {
      throw new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
    }
  }

  public void updateLastReadMessageIfExists(ChatRoom chatRoom, ChatRoom.Participant participant) {
    if (chatRoom.getLastMessage() != null) {
      participant.updateLastReadMessage(chatRoom.getLastMessage().getId());
    }
  }
}
