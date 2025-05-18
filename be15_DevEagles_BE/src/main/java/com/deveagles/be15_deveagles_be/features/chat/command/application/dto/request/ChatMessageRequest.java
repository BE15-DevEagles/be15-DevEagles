package com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage.MessageType;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
  private String chatroomId;
  private String senderId;
  private String senderName;
  private MessageType messageType;
  private String content;
  private Map<String, Object> metadata;
}
