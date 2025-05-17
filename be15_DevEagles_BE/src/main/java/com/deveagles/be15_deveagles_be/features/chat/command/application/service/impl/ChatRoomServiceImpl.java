package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.CreateChatRoomRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatRoomResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.factory.ChatRoomFactory;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import java.util.ArrayList;
import java.util.Optional;
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
}
