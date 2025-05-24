package com.deveagles.be15_deveagles_be.features.chat.command.application.service.util;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ParticipantValidator {

  public ChatRoom.Participant validateAndGetParticipant(ChatRoom chatRoom, String userId) {
    ChatRoom.Participant participant = chatRoom.getParticipant(userId);
    if (participant == null) {
      throw new ChatBusinessException(ChatErrorCode.PARTICIPANT_NOT_FOUND);
    }
    return participant;
  }

  public void validateParticipantNotExists(ChatRoom chatRoom, String userId) {
    if (chatRoom.getParticipant(userId) != null) {
      throw new ChatBusinessException(ChatErrorCode.PARTICIPANT_ALREADY_EXISTS);
    }
  }
}
