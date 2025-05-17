package com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chatroom")
@Getter
@Builder
public class ChatRoom {

  @Id private String id;

  private String teamId;

  private String name;

  @Builder.Default private boolean isDefault = false;

  private ChatRoomType type;

  private LastMessageInfo lastMessage;

  private LocalDateTime createdAt;

  private LocalDateTime deletedAt;

  @Getter
  @Builder
  public static class LastMessageInfo {
    private String id;
    private String content;
    private String senderId;
    private String senderName;
    private LocalDateTime sentAt;
  }

  public enum ChatRoomType {
    GROUP,
    DIRECT,
    TEAM
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void updateLastMessage(LastMessageInfo lastMessage) {
    this.lastMessage = lastMessage;
  }
}
