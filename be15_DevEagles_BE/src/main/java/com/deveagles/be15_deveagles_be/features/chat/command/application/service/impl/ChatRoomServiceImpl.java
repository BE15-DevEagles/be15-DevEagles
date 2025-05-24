package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.CreateChatRoomRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatRoomResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.NotificationSettingResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.NotificationToggleResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.util.ChatRoomHelper;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.util.ChatRoomOperationHelper;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.util.ChatRoomValidator;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.util.ParticipantValidator;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
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
  private final ChatRoomValidator chatRoomValidator;
  private final ParticipantValidator participantValidator;
  private final ChatRoomHelper chatRoomHelper;
  private final ChatRoomOperationHelper operationHelper;

  public ChatRoomServiceImpl(
      ChatRoomRepository chatRoomRepository,
      ChatRoomValidator chatRoomValidator,
      ParticipantValidator participantValidator,
      ChatRoomHelper chatRoomHelper,
      ChatRoomOperationHelper operationHelper) {
    this.chatRoomRepository = chatRoomRepository;
    this.chatRoomValidator = chatRoomValidator;
    this.participantValidator = participantValidator;
    this.chatRoomHelper = chatRoomHelper;
    this.operationHelper = operationHelper;
  }

  @Override
  @Transactional
  public ChatRoomResponse createChatRoom(CreateChatRoomRequest request) {
    if (request.isDefault()) {
      chatRoomValidator.validateDefaultChatRoomNotExists(request.getTeamId());
    }

    ChatRoom chatRoom =
        ChatRoomFactory.createRegularChatRoom(
            request.getTeamId(),
            request.getName(),
            request.getType(),
            request.isDefault(),
            request.getParticipantIds());

    return chatRoomHelper.saveAndConvertToResponse(chatRoom);
  }

  @Override
  @Transactional
  public ChatRoomResponse createDefaultChatRoom(String teamId, String name) {
    chatRoomValidator.validateDefaultChatRoomNotExists(teamId);

    ChatRoom chatRoom = ChatRoomFactory.createDefaultChatRoom(teamId, name, new ArrayList<>());
    return chatRoomHelper.saveAndConvertToResponse(chatRoom);
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
    return chatRoomHelper.saveAndConvertToResponse(chatRoom);
  }

  @Override
  @Transactional
  public Optional<ChatRoomResponse> deleteChatRoom(String chatroomId) {
    Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(chatroomId);

    if (chatRoomOptional.isPresent()) {
      ChatRoom chatRoom = chatRoomOptional.get();
      chatRoomValidator.validateNotDefaultChatRoom(chatRoom);

      chatRoom.delete();
      return Optional.of(chatRoomHelper.saveAndConvertToResponse(chatRoom));
    }

    return Optional.empty();
  }

  @Override
  @Transactional
  public ChatRoomResponse addParticipantToChatRoom(String chatroomId, String userId) {
    ChatRoom chatRoom = chatRoomValidator.validateAndGetChatRoom(chatroomId);
    participantValidator.validateParticipantNotExists(chatRoom, userId);

    return chatRoomHelper.executeAndSave(
        chatRoom, room -> room.addParticipant(userId), ChatErrorCode.PARTICIPANT_ADD_FAILED);
  }

  @Override
  @Transactional
  public ChatRoomResponse removeParticipantFromChatRoom(String chatroomId, String userId) {
    ChatRoom chatRoom = chatRoomValidator.validateAndGetChatRoom(chatroomId);
    participantValidator.validateAndGetParticipant(chatRoom, userId);

    return chatRoomHelper.executeAndSave(
        chatRoom, room -> room.removeParticipant(userId), ChatErrorCode.PARTICIPANT_REMOVE_FAILED);
  }

  @Override
  @Transactional
  public ChatRoomResponse toggleParticipantNotification(String chatroomId, String userId) {
    ChatRoom chatRoom = chatRoomValidator.validateAndGetChatRoom(chatroomId);
    ChatRoom.Participant participant =
        participantValidator.validateAndGetParticipant(chatRoom, userId);

    return chatRoomHelper.executeParticipantOperationAndSave(
        chatRoom,
        participant,
        ChatRoom.Participant::toggleNotification,
        ChatErrorCode.PARTICIPANT_NOTIFICATION_TOGGLE_FAILED);
  }

  @Override
  @Transactional
  public ChatRoomResponse updateLastReadMessage(
      String chatroomId, String userId, String messageId) {
    ChatRoom chatRoom = chatRoomValidator.validateAndGetChatRoom(chatroomId);
    ChatRoom.Participant participant =
        participantValidator.validateAndGetParticipant(chatRoom, userId);

    return chatRoomHelper.executeParticipantOperationAndSave(
        chatRoom,
        participant,
        p -> p.updateLastReadMessage(messageId),
        ChatErrorCode.PARTICIPANT_NOTIFICATION_TOGGLE_FAILED);
  }

  @Override
  @Transactional
  public void markChatRoomAsRead(String chatroomId, String userId) {
    ChatRoom chatRoom = chatRoomValidator.validateAndGetChatRoom(chatroomId);
    ChatRoom.Participant participant =
        participantValidator.validateAndGetParticipant(chatRoom, userId);

    operationHelper.executeWithExceptionHandling(
        () -> {
          operationHelper.updateLastReadMessageIfExists(chatRoom, participant);
          chatRoomRepository.save(chatRoom);
        },
        ChatErrorCode.PARTICIPANT_NOTIFICATION_TOGGLE_FAILED,
        "채팅방 읽음 처리 실패");
  }

  @Override
  @Transactional(readOnly = true)
  public Boolean getChatNotificationSetting(String chatroomId, String userId) {
    ChatRoom chatRoom = chatRoomValidator.validateAndGetChatRoom(chatroomId);
    ChatRoom.Participant participant =
        participantValidator.validateAndGetParticipant(chatRoom, userId);

    return participant.isNotificationEnabled();
  }

  @Override
  @Transactional
  public NotificationToggleResponse toggleCurrentUserNotification(
      String chatroomId, String userId) {
    ChatRoom chatRoom = chatRoomValidator.validateAndGetChatRoom(chatroomId);
    ChatRoom.Participant participant =
        participantValidator.validateAndGetParticipant(chatRoom, userId);

    operationHelper.executeWithExceptionHandling(
        () -> {
          participant.toggleNotification();
          chatRoomRepository.save(chatRoom);
        },
        ChatErrorCode.PARTICIPANT_NOTIFICATION_TOGGLE_FAILED,
        "알림 설정 변경 실패");

    return NotificationToggleResponse.of(participant.isNotificationEnabled());
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
}
