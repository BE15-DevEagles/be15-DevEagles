package com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationToggleResponse {
  private boolean notificationEnabled;

  public static NotificationToggleResponse of(boolean notificationEnabled) {
    return NotificationToggleResponse.builder().notificationEnabled(notificationEnabled).build();
  }
}
