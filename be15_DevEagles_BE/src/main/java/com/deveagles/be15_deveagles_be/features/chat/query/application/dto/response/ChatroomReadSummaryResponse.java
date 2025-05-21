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
public class ChatroomReadSummaryResponse {
  private String lastMessageId;
  private LocalDateTime lastMessageTime;
  private Boolean readByAll;
  private Integer readCount;
  private Integer totalParticipants;
  private List<UnreadByUserDto> unreadByUsers;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UnreadByUserDto {
    private String userId;
    private String userName;
  }
}
