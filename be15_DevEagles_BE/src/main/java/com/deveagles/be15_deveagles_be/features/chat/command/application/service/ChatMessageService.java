package com.deveagles.be15_deveagles_be.features.chat.command.application.service;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import java.util.List;
import java.util.Optional;

public interface ChatMessageService {
  ChatMessageResponse sendMessage(ChatMessageRequest request);

  Optional<ChatMessageResponse> getMessage(String messageId);

  List<ChatMessageResponse> getMessagesByChatroom(String chatroomId, int page, int size);

  Optional<ChatMessageResponse> deleteMessage(String messageId);
}
