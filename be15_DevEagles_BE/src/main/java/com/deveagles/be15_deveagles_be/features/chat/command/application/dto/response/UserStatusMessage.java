package com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class UserStatusMessage {
  private final String userId;
  private final boolean online;
  private final long timestamp;

  public UserStatusMessage(String userId, boolean online) {
    this.userId = userId;
    this.online = online;
    this.timestamp = System.currentTimeMillis();
  }
}
