package com.deveagles.be15_deveagles_be.features.chat.query.application.service.util;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageResponse;
import org.springframework.stereotype.Component;

@Component
public class MessageResponseConverter {

  /**
   * ChatMessage를 MessageResponse로 변환
   *
   * @param message 변환할 ChatMessage
   * @return MessageResponse
   */
  public MessageResponse convertFromChatMessage(ChatMessage message) {
    return MessageResponse.builder()
        .id(message.getId())
        .chatroomId(message.getChatroomId())
        .senderId(message.getSenderId())
        .senderName(message.getSenderName())
        .messageType(message.getMessageType().name())
        .content(message.getContent())
        .metadata(message.getMetadata())
        .createdAt(message.getCreatedAt())
        .deletedAt(message.getDeletedAt())
        .build();
  }

  /**
   * ChatMessageResponse를 MessageResponse로 변환
   *
   * @param message 변환할 ChatMessageResponse
   * @return MessageResponse
   */
  public MessageResponse convertFromChatMessageResponse(ChatMessageResponse message) {
    return MessageResponse.builder()
        .id(message.getId())
        .chatroomId(message.getChatroomId())
        .senderId(message.getSenderId())
        .senderName(message.getSenderName())
        .messageType(message.getMessageType().name())
        .content(message.getContent())
        .metadata(message.getMetadata())
        .createdAt(message.getCreatedAt())
        .deletedAt(message.isDeleted() ? message.getCreatedAt() : null)
        .build();
  }
}
