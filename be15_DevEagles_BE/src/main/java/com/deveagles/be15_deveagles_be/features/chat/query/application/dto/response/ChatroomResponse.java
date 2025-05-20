package com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatroomResponse {
  private String id;
  private String teamId;
  private String name;
  private Boolean isDefault;
  private String type; // ENUM(GROUP, DIRECT, TEAM, AI)
  private LastMessageDto lastMessage;
  private List<ParticipantDto> participants;
  private LocalDateTime createdAt;
  private Boolean isDeleted;

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
    private Boolean notificationEnabled;
    private LocalDateTime createdAt;
  }
}
