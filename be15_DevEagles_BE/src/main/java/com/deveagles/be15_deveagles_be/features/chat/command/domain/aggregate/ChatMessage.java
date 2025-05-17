package com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat_message")
@Getter
@Builder
public class ChatMessage {

  @Id private String id;

  private String chatroomId;

  private String senderId;

  private String senderName;

  private MessageType messageType;

  private String content;

  private Map<String, Object> metadata;

  private LocalDateTime createdAt;

  private LocalDateTime deletedAt;

  public enum MessageType {
    TEXT,
    IMAGE,
    FILE,
    SYSTEM
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }
}
