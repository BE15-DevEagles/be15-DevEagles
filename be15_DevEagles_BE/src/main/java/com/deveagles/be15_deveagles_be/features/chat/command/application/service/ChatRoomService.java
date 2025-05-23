package com.deveagles.be15_deveagles_be.features.chat.command.application.service;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.CreateChatRoomRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatRoomResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.NotificationSettingResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.NotificationToggleResponse;
import java.util.List;
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

  void markChatRoomAsRead(String chatroomId, String userId);

  // 새로운 알림 설정 관련 메소드들
  Boolean getChatNotificationSetting(String chatroomId, String userId);

  NotificationToggleResponse toggleCurrentUserNotification(String chatroomId, String userId);

  List<NotificationSettingResponse> getAllNotificationSettings(String userId);
}
