package com.deveagles.be15_deveagles_be.features.chat.command.application.service;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.CreateChatRoomRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatRoomResponse;
import java.util.Optional;

public interface ChatRoomService {

  ChatRoomResponse createChatRoom(CreateChatRoomRequest request);

  ChatRoomResponse createDefaultChatRoom(String teamId, String name);

  ChatRoomResponse createOrGetPersonalAiChatRoom(String teamId, String userId, String name);

  Optional<ChatRoomResponse> deleteChatRoom(String chatroomId);

  ChatRoomResponse addParticipantToChatRoom(String chatroomId, String userId);

  ChatRoomResponse removeParticipantFromChatRoom(String chatroomId, String userId);

  ChatRoomResponse toggleParticipantNotification(String chatroomId, String userId);

  ChatRoomResponse updateLastReadMessage(String chatroomId, String userId, String messageId);
}
