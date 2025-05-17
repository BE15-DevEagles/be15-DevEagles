package com.deveagles.be15_deveagles_be.features.chat.command.domain.entity;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chatroom_participants")
@Getter
@Builder
public class ChatroomParticipant {

  @Id private String id;

  private String chatroomId;

  private String userId;

  private String lastReadMessageId;

  @Builder.Default private boolean notificationEnabled = true;

  private LocalDateTime createdAt;

  private LocalDateTime deletedAt;

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void updateLastReadMessage(String messageId) {
    this.lastReadMessageId = messageId;
  }

  public void toggleNotification() {
    this.notificationEnabled = !this.notificationEnabled;
  }
}
