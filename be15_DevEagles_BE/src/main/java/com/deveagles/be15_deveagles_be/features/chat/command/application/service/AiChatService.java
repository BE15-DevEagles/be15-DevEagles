package com.deveagles.be15_deveagles_be.features.chat.command.application.service;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;

public interface AiChatService {

  ChatMessageResponse processUserMessage(ChatMessageRequest userMessage);

  void initializeAiChatSession(String userId, String chatroomId);

  void terminateAiChatSession(String userId, String chatroomId);
}
