package com.deveagles.be15_deveagles_be.features.chat.command.application.controller;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatRoomResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

  private final ChatMessageService chatMessageService;
  private final ChatRoomService chatRoomService;
  private final SimpMessagingTemplate messagingTemplate;

  public ChatWebSocketController(
      ChatMessageService chatMessageService,
      ChatRoomService chatRoomService,
      SimpMessagingTemplate messagingTemplate) {
    this.chatMessageService = chatMessageService;
    this.chatRoomService = chatRoomService;
    this.messagingTemplate = messagingTemplate;
  }

  @MessageMapping("/chat.send")
  public ChatMessageResponse sendMessage(
      @Payload ChatMessageRequest request,
      SimpMessageHeaderAccessor headerAccessor,
      Principal principal) {
    if (principal != null && !principal.getName().equals(request.getSenderId())) {
      request =
          ChatMessageRequest.builder()
              .chatroomId(request.getChatroomId())
              .senderId(principal.getName())
              .senderName(request.getSenderName())
              .messageType(request.getMessageType())
              .content(request.getContent())
              .metadata(request.getMetadata())
              .build();
    }

    return chatMessageService.sendMessage(request);
  }

  @MessageMapping("/chat.read")
  public void markAsRead(@Payload ReadMessageRequest request, Principal principal) {
    if (principal == null) {
      return;
    }

    ChatRoomResponse chatRoom =
        chatRoomService.updateLastReadMessage(
            request.getChatroomId(), principal.getName(), request.getMessageId());

    messagingTemplate.convertAndSend(
        "/topic/chatroom." + request.getChatroomId() + ".read",
        new ReadStatusResponse(
            request.getChatroomId(), principal.getName(), request.getMessageId()));
  }

  public static class ReadMessageRequest {
    private String chatroomId;
    private String messageId;

    public String getChatroomId() {
      return chatroomId;
    }

    public String getMessageId() {
      return messageId;
    }
  }

  public static class ReadStatusResponse {
    private String chatroomId;
    private String userId;
    private String lastReadMessageId;

    public ReadStatusResponse(String chatroomId, String userId, String lastReadMessageId) {
      this.chatroomId = chatroomId;
      this.userId = userId;
      this.lastReadMessageId = lastReadMessageId;
    }

    public String getChatroomId() {
      return chatroomId;
    }

    public String getUserId() {
      return userId;
    }

    public String getLastReadMessageId() {
      return lastReadMessageId;
    }
  }
}
