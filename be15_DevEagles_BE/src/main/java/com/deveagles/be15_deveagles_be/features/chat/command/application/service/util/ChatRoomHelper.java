package com.deveagles.be15_deveagles_be.features.chat.command.application.service.util;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatRoomResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class ChatRoomHelper {

  private final ChatRoomRepository chatRoomRepository;

  public ChatRoomHelper(ChatRoomRepository chatRoomRepository) {
    this.chatRoomRepository = chatRoomRepository;
  }

  public ChatRoomResponse saveAndConvertToResponse(ChatRoom chatRoom) {
    ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
    return ChatRoomResponse.from(savedChatRoom);
  }

  public ChatRoomResponse executeAndSave(
      ChatRoom chatRoom, Consumer<ChatRoom> operation, ChatErrorCode errorCode) {
    try {
      operation.accept(chatRoom);
      return saveAndConvertToResponse(chatRoom);
    } catch (Exception e) {
      throw new ChatBusinessException(errorCode, e.getMessage());
    }
  }

  public ChatRoomResponse executeParticipantOperationAndSave(
      ChatRoom chatRoom,
      ChatRoom.Participant participant,
      Consumer<ChatRoom.Participant> operation,
      ChatErrorCode errorCode) {
    try {
      operation.accept(participant);
      return saveAndConvertToResponse(chatRoom);
    } catch (Exception e) {
      throw new ChatBusinessException(errorCode, e.getMessage());
    }
  }
}
