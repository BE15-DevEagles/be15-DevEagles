package com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
  private String id;
  private String teamId;
  private String name;
  private boolean isDefault;
  private ChatRoomType type;
  private LastMessageDto lastMessage;
  private LocalDateTime createdAt;
  private boolean isDeleted;
  private String userId;
  private List<ParticipantDto> participants;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LastMessageDto {
    private String id;
    private String content;
    private String senderId;
    private String senderName;
    private LocalDateTime sentAt;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ParticipantDto {
    private String userId;
    private String lastReadMessageId;
    private boolean notificationEnabled;
    private LocalDateTime createdAt;
  }

  public static ChatRoomResponse from(ChatRoom chatRoom) {
    return ChatRoomResponse.builder()
        .id(chatRoom.getId())
        .teamId(chatRoom.getTeamId())
        .name(chatRoom.getName())
        .isDefault(chatRoom.isDefault())
        .type(chatRoom.getType())
        .userId(chatRoom.getUserId())
        .lastMessage(convertLastMessage(chatRoom.getLastMessage()))
        .participants(convertParticipants(chatRoom.getActiveParticipants()))
        .createdAt(chatRoom.getCreatedAt())
        .isDeleted(chatRoom.isDeleted())
        .build();
  }

  private static LastMessageDto convertLastMessage(ChatRoom.LastMessageInfo lastMessage) {
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

  private static List<ParticipantDto> convertParticipants(List<ChatRoom.Participant> participants) {
    if (participants == null || participants.isEmpty()) {
      return new ArrayList<>();
    }

    return participants.stream()
        .map(
            p ->
                ParticipantDto.builder()
                    .userId(p.getUserId())
                    .lastReadMessageId(p.getLastReadMessageId())
                    .notificationEnabled(p.isNotificationEnabled())
                    .createdAt(p.getCreatedAt())
                    .build())
        .collect(Collectors.toList());
  }
}
