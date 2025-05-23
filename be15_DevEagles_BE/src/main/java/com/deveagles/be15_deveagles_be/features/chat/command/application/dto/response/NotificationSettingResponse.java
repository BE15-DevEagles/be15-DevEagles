package com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationSettingResponse {
  private String chatRoomId;
  private boolean notificationEnabled;

  public static NotificationSettingResponse of(String chatRoomId, boolean notificationEnabled) {
    return NotificationSettingResponse.builder()
        .chatRoomId(chatRoomId)
        .notificationEnabled(notificationEnabled)
        .build();
  }
}
