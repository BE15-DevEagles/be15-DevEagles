package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import com.deveagles.be15_deveagles_be.common.dto.PagedResult;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatMessageRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final SimpMessagingTemplate messagingTemplate;

  public ChatMessageServiceImpl(
      ChatMessageRepository chatMessageRepository,
      ChatRoomRepository chatRoomRepository,
      SimpMessagingTemplate messagingTemplate) {
    this.chatMessageRepository = chatMessageRepository;
    this.chatRoomRepository = chatRoomRepository;
    this.messagingTemplate = messagingTemplate;
  }

  @Override
  @Transactional
  public ChatMessageResponse sendMessage(ChatMessageRequest request) {
    ChatRoom chatRoom =
        chatRoomRepository
            .findById(request.getChatroomId())
            .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    boolean isParticipant =
        chatRoom.getActiveParticipants().stream()
            .anyMatch(participant -> participant.getUserId().equals(request.getSenderId()));

    if (!isParticipant) {
      throw new ChatBusinessException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
    }

    ChatMessage message =
        ChatMessage.builder()
            .chatroomId(request.getChatroomId())
            .senderId(request.getSenderId())
            .senderName(request.getSenderName())
            .messageType(request.getMessageType())
            .content(request.getContent())
            .metadata(request.getMetadata())
            .createdAt(LocalDateTime.now())
            .build();

    ChatMessage savedMessage = chatMessageRepository.save(message);

    updateChatRoomLastMessage(chatRoom, savedMessage);

    ChatMessageResponse response = ChatMessageResponse.from(savedMessage);
    messagingTemplate.convertAndSend("/topic/chatroom." + request.getChatroomId(), response);

    return response;
  }

  @Override
  public Optional<ChatMessageResponse> getMessage(String messageId) {
    return chatMessageRepository.findById(messageId).map(ChatMessageResponse::from);
  }

  @Override
  public List<ChatMessageResponse> getMessagesByChatroom(String chatroomId, int page, int size) {
    chatRoomRepository
        .findById(chatroomId)
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    PagedResult<ChatMessage> messages =
        chatMessageRepository.findMessagesByChatroomIdWithPagination(chatroomId, page, size);

    return messages.getContent().stream()
        .map(ChatMessageResponse::from)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Optional<ChatMessageResponse> deleteMessage(String messageId) {
    Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);

    if (messageOpt.isEmpty()) {
      return Optional.empty();
    }

    ChatMessage message = messageOpt.get();
    message.delete();
    ChatMessage updatedMessage = chatMessageRepository.save(message);

    ChatMessageResponse response = ChatMessageResponse.from(updatedMessage);
    messagingTemplate.convertAndSend(
        "/topic/chatroom." + message.getChatroomId() + ".delete", response);

    return Optional.of(response);
  }

  private void updateChatRoomLastMessage(ChatRoom chatRoom, ChatMessage message) {
    ChatRoom.LastMessageInfo lastMessageInfo =
        ChatRoom.LastMessageInfo.builder()
            .id(message.getId())
            .content(message.getContent())
            .senderId(message.getSenderId())
            .senderName(message.getSenderName())
            .sentAt(message.getCreatedAt())
            .build();

    chatRoom.updateLastMessage(lastMessageInfo);
    chatRoomRepository.save(chatRoom);
  }
}
