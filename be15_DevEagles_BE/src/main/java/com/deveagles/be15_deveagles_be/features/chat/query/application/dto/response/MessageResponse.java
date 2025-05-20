package com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response;

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
public class MessageResponse {
  private String id;
  private String chatroomId;
  private String senderId;
  private String senderName;
  private String messageType; // ENUM(TEXT, IMAGE, FILE, SYSTEM)
  private String content;
  private Map<String, Object> metadata;
  private LocalDateTime createdAt;
  private LocalDateTime deletedAt;
}
