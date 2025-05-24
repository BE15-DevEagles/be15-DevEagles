package com.deveagles.be15_deveagles_be.features.chat.query.application.service.util;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.Participant;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomResponse.LastMessageDto;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomResponse.ParticipantDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ChatroomResponseConverter {

  /**
   * ChatRoom을 ChatroomResponse로 변환
   *
   * @param chatRoom 변환할 채팅방
   * @return ChatroomResponse
   */
  public ChatroomResponse convertToChatroomResponse(ChatRoom chatRoom) {
    List<ParticipantDto> participants = convertParticipants(chatRoom.getActiveParticipants());
    LastMessageDto lastMessageDto = convertLastMessage(chatRoom.getLastMessage());

    return ChatroomResponse.builder()
        .id(chatRoom.getId())
        .teamId(chatRoom.getTeamId())
        .name(chatRoom.getName())
        .isDefault(chatRoom.isDefault())
        .type(chatRoom.getType().name())
        .lastMessage(lastMessageDto)
        .participants(participants)
        .createdAt(chatRoom.getCreatedAt())
        .isDeleted(chatRoom.isDeleted())
        .build();
  }

  /**
   * 참가자 목록 변환
   *
   * @param participants 참가자 목록
   * @return ParticipantDto 목록
   */
  public List<ParticipantDto> convertParticipants(List<Participant> participants) {
    return participants.stream().map(this::convertParticipantToDto).collect(Collectors.toList());
  }

  /**
   * 참가자를 DTO로 변환
   *
   * @param participant 참가자
   * @return ParticipantDto
   */
  public ParticipantDto convertParticipantToDto(Participant participant) {
    return ParticipantDto.builder()
        .userId(participant.getUserId())
        .lastReadMessageId(participant.getLastReadMessageId())
        .notificationEnabled(participant.isNotificationEnabled())
        .createdAt(participant.getCreatedAt())
        .build();
  }

  /**
   * 마지막 메시지를 DTO로 변환
   *
   * @param lastMessage 마지막 메시지
   * @return LastMessageDto
   */
  public LastMessageDto convertLastMessage(ChatRoom.LastMessageInfo lastMessage) {
    if (lastMessage == null) {
      return null;
    }

    return LastMessageDto.builder()
        .id(lastMessage.getId())
        .content(lastMessage.getContent())
        .senderId(lastMessage.getSenderId())
        .senderName(lastMessage.getSenderName())
        .sentAt(lastMessage.getSentAt())
        .build();
  }
}
