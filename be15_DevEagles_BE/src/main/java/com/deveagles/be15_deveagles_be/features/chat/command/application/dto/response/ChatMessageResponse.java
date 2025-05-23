package com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage.MessageType;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
  private String id;
  private String chatroomId;
  private String senderId;
  private String senderName;
  private MessageType messageType;
  private String content;
  private Map<String, Object> metadata;
  private LocalDateTime createdAt;
  private LocalDateTime timestamp;
  private boolean isDeleted;

  public static ChatMessageResponse from(ChatMessage message) {
    return ChatMessageResponse.builder()
        .id(message.getId())
        .chatroomId(message.getChatroomId())
        .senderId(message.getSenderId())
        .senderName(message.getSenderName())
        .messageType(message.getMessageType())
        .content(message.getContent())
        .metadata(message.getMetadata())
        .createdAt(message.getCreatedAt())
        .timestamp(message.getCreatedAt())
        .isDeleted(message.isDeleted())
        .build();
  }
}
