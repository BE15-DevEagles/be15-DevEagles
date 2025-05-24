package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.CreateChatRoomRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatRoomResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.NotificationSettingResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.NotificationToggleResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.factory.ChatRoomFactory;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {

  private final ChatRoomRepository chatRoomRepository;

  public ChatRoomServiceImpl(ChatRoomRepository chatRoomRepository) {
    this.chatRoomRepository = chatRoomRepository;
  }

  @Override
  @Transactional
  public ChatRoomResponse createChatRoom(CreateChatRoomRequest request) {
    if (request.isDefault()) {
      Optional<ChatRoom> existingDefaultChatRoom =
          chatRoomRepository.findDefaultChatRoomByTeamId(request.getTeamId());
      if (existingDefaultChatRoom.isPresent()) {
        throw new ChatBusinessException(ChatErrorCode.DEFAULT_CHATROOM_ALREADY_EXISTS);
      }
    }

    ChatRoom chatRoom =
        ChatRoomFactory.createRegularChatRoom(
            request.getTeamId(),
            request.getName(),
            request.getType(),
            request.isDefault(),
            request.getParticipantIds());

    ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

    return ChatRoomResponse.from(savedChatRoom);
  }

  @Override
  @Transactional
  public ChatRoomResponse createDefaultChatRoom(String teamId, String name) {
    Optional<ChatRoom> existingDefaultChatRoom =
        chatRoomRepository.findDefaultChatRoomByTeamId(teamId);
    if (existingDefaultChatRoom.isPresent()) {
      throw new ChatBusinessException(ChatErrorCode.DEFAULT_CHATROOM_ALREADY_EXISTS);
    }

    ChatRoom chatRoom = ChatRoomFactory.createDefaultChatRoom(teamId, name, new ArrayList<>());
    ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

    return ChatRoomResponse.from(savedChatRoom);
  }

  @Override
  @Transactional
  public ChatRoomResponse createOrGetPersonalAiChatRoom(String teamId, String userId, String name) {
    Optional<ChatRoom> existingAiChatRoom =
        chatRoomRepository.findByTeamIdAndUserIdAndTypeAndDeletedAtIsNull(
            teamId, userId, ChatRoomType.AI);

    if (existingAiChatRoom.isPresent()) {
      return ChatRoomResponse.from(existingAiChatRoom.get());
    }

    ChatRoom chatRoom = ChatRoomFactory.createAiChatRoom(teamId, userId, name);
    ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

    return ChatRoomResponse.from(savedChatRoom);
  }

  @Override
  @Transactional
  public Optional<ChatRoomResponse> deleteChatRoom(String chatroomId) {
    Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(chatroomId);

    if (chatRoomOptional.isPresent()) {
      ChatRoom chatRoom = chatRoomOptional.get();

      if (chatRoom.isDefault()) {
        throw new ChatBusinessException(ChatErrorCode.DEFAULT_CHATROOM_CANNOT_BE_DELETED);
      }

      chatRoom.delete();
      ChatRoom deletedChatRoom = chatRoomRepository.save(chatRoom);

      return Optional.of(ChatRoomResponse.from(deletedChatRoom));
    }

    return Optional.empty();
  }

  @Override
  @Transactional
  public ChatRoomResponse addParticipantToChatRoom(String chatroomId, String userId) {
    ChatRoom chatRoom = getChatRoomById(chatroomId);

    if (chatRoom.getParticipant(userId) != null) {
      throw new ChatBusinessException(ChatErrorCode.PARTICIPANT_ALREADY_EXISTS);
    }

    try {
      chatRoom.addParticipant(userId);
      ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
      return ChatRoomResponse.from(savedChatRoom);
    } catch (Exception e) {
      throw new ChatBusinessException(ChatErrorCode.PARTICIPANT_ADD_FAILED, e.getMessage());
    }
  }

  @Override
  @Transactional
  public ChatRoomResponse removeParticipantFromChatRoom(String chatroomId, String userId) {
    ChatRoom chatRoom = getChatRoomById(chatroomId);

    if (chatRoom.getParticipant(userId) == null) {
      throw new ChatBusinessException(ChatErrorCode.PARTICIPANT_NOT_FOUND);
    }

    try {
      chatRoom.removeParticipant(userId);
      ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
      return ChatRoomResponse.from(savedChatRoom);
    } catch (Exception e) {
      throw new ChatBusinessException(ChatErrorCode.PARTICIPANT_REMOVE_FAILED, e.getMessage());
    }
  }

  @Override
  @Transactional
  public ChatRoomResponse toggleParticipantNotification(String chatroomId, String userId) {
    ChatRoom chatRoom = getChatRoomById(chatroomId);

    ChatRoom.Participant participant = chatRoom.getParticipant(userId);
    if (participant == null) {
      throw new ChatBusinessException(ChatErrorCode.PARTICIPANT_NOT_FOUND);
    }

    try {
      participant.toggleNotification();
      ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
      return ChatRoomResponse.from(savedChatRoom);
    } catch (Exception e) {
      throw new ChatBusinessException(
          ChatErrorCode.PARTICIPANT_NOTIFICATION_TOGGLE_FAILED, e.getMessage());
    }
  }

  @Override
  @Transactional
  public ChatRoomResponse updateLastReadMessage(
      String chatroomId, String userId, String messageId) {
    ChatRoom chatRoom = getChatRoomById(chatroomId);

    ChatRoom.Participant participant = chatRoom.getParticipant(userId);
    if (participant == null) {
      throw new ChatBusinessException(ChatErrorCode.PARTICIPANT_NOT_FOUND);
    }

    try {
      participant.updateLastReadMessage(messageId);
      ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
      return ChatRoomResponse.from(savedChatRoom);
    } catch (Exception e) {
      throw new ChatBusinessException(
          ChatErrorCode.PARTICIPANT_NOTIFICATION_TOGGLE_FAILED, e.getMessage());
    }
  }

  @Override
  @Transactional
  public void markChatRoomAsRead(String chatroomId, String userId) {
    ChatRoom chatRoom = getChatRoomById(chatroomId);

    ChatRoom.Participant participant = chatRoom.getParticipant(userId);
    if (participant == null) {
      throw new ChatBusinessException(ChatErrorCode.PARTICIPANT_NOT_FOUND);
    }

    try {
      // 채팅방의 마지막 메시지를 읽음 처리
      if (chatRoom.getLastMessage() != null) {
        participant.updateLastReadMessage(chatRoom.getLastMessage().getId());
        chatRoomRepository.save(chatRoom);
      }
    } catch (Exception e) {
      throw new ChatBusinessException(
          ChatErrorCode.PARTICIPANT_NOTIFICATION_TOGGLE_FAILED, e.getMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Boolean getChatNotificationSetting(String chatroomId, String userId) {
    ChatRoom chatRoom = getChatRoomById(chatroomId);

    ChatRoom.Participant participant = chatRoom.getParticipant(userId);
    if (participant == null) {
      throw new ChatBusinessException(ChatErrorCode.PARTICIPANT_NOT_FOUND);
    }

    return participant.isNotificationEnabled();
  }

  @Override
  @Transactional
  public NotificationToggleResponse toggleCurrentUserNotification(
      String chatroomId, String userId) {
    ChatRoom chatRoom = getChatRoomById(chatroomId);

    ChatRoom.Participant participant = chatRoom.getParticipant(userId);
    if (participant == null) {
      throw new ChatBusinessException(ChatErrorCode.PARTICIPANT_NOT_FOUND);
    }

    try {
      participant.toggleNotification();
      chatRoomRepository.save(chatRoom);
      return NotificationToggleResponse.of(participant.isNotificationEnabled());
    } catch (Exception e) {
      throw new ChatBusinessException(
          ChatErrorCode.PARTICIPANT_NOTIFICATION_TOGGLE_FAILED, e.getMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<NotificationSettingResponse> getAllNotificationSettings(String userId) {
    List<ChatRoom> chatRooms =
        chatRoomRepository.findByParticipantsUserIdAndDeletedAtIsNull(userId);

    return chatRooms.stream()
        .map(
            chatRoom -> {
              ChatRoom.Participant participant = chatRoom.getParticipant(userId);
              if (participant != null && !participant.isDeleted()) {
                return NotificationSettingResponse.of(
                    chatRoom.getId(), participant.isNotificationEnabled());
              }
              return null;
            })
        .filter(response -> response != null)
        .collect(Collectors.toList());
  }

  private ChatRoom getChatRoomById(String chatroomId) {
    Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(chatroomId);

    if (chatRoomOptional.isEmpty() || chatRoomOptional.get().isDeleted()) {
      throw new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
    }

    return chatRoomOptional.get();
  }
}
