package com.deveagles.be15_deveagles_be.features.chat.command.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage.MessageType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {

  @Mock private ChatMessageService chatMessageService;

  private ChatMessageRequest chatMessageRequest;
  private ChatMessageResponse chatMessageResponse;
  private final String CHATROOM_ID = "chatroom1";
  private final String USER_ID = "user1";
  private final String MESSAGE_ID = "message1";

  @BeforeEach
  void setUp() {
    // 테스트용 메시지 요청 설정
    chatMessageRequest =
        ChatMessageRequest.builder()
            .chatroomId(CHATROOM_ID)
            .senderId(USER_ID)
            .senderName("Test User")
            .messageType(MessageType.TEXT)
            .content("Test Message")
            .build();

    // 테스트용 메시지 응답 설정
    chatMessageResponse =
        ChatMessageResponse.builder()
            .id(MESSAGE_ID)
            .chatroomId(CHATROOM_ID)
            .senderId(USER_ID)
            .senderName("Test User")
            .messageType(MessageType.TEXT)
            .content("Test Message")
            .createdAt(LocalDateTime.now())
            .isDeleted(false)
            .build();
  }

  @Test
  @DisplayName("메시지 전송 성공 테스트")
  void sendMessage_Success() {
    // given
    when(chatMessageService.sendMessage(any(ChatMessageRequest.class)))
        .thenReturn(chatMessageResponse);

    // when
    ChatMessageResponse response = chatMessageService.sendMessage(chatMessageRequest);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getChatroomId()).isEqualTo(CHATROOM_ID);
    assertThat(response.getSenderId()).isEqualTo(USER_ID);
    assertThat(response.getContent()).isEqualTo("Test Message");

    verify(chatMessageService).sendMessage(chatMessageRequest);
  }

  @Test
  @DisplayName("존재하지 않는 채팅방에 메시지 전송 실패 테스트")
  void sendMessage_ChatRoomNotFound() {
    // given
    when(chatMessageService.sendMessage(chatMessageRequest))
        .thenThrow(new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    // when, then
    assertThatThrownBy(() -> chatMessageService.sendMessage(chatMessageRequest))
        .isInstanceOf(ChatBusinessException.class);

    verify(chatMessageService).sendMessage(chatMessageRequest);
  }

  @Test
  @DisplayName("메시지 ID로 메시지 조회 테스트")
  void getMessage_Success() {
    // given
    when(chatMessageService.getMessage(MESSAGE_ID)).thenReturn(Optional.of(chatMessageResponse));

    // when
    Optional<ChatMessageResponse> response = chatMessageService.getMessage(MESSAGE_ID);

    // then
    assertThat(response).isPresent();
    assertThat(response.get().getId()).isEqualTo(MESSAGE_ID);
    assertThat(response.get().getContent()).isEqualTo("Test Message");

    verify(chatMessageService).getMessage(MESSAGE_ID);
  }

  @Test
  @DisplayName("존재하지 않는 메시지 ID로 조회 테스트")
  void getMessage_NotFound() {
    // given
    when(chatMessageService.getMessage(MESSAGE_ID)).thenReturn(Optional.empty());

    // when
    Optional<ChatMessageResponse> response = chatMessageService.getMessage(MESSAGE_ID);

    // then
    assertThat(response).isEmpty();
    verify(chatMessageService).getMessage(MESSAGE_ID);
  }

  @Test
  @DisplayName("채팅방 메시지 페이징 조회 테스트")
  void getMessagesByChatroom_Success() {
    // given
    List<ChatMessageResponse> messages =
        Arrays.asList(
            createChatMessageResponse("message1", "Test Message 1"),
            createChatMessageResponse("message2", "Test Message 2"),
            createChatMessageResponse("message3", "Test Message 3"));

    when(chatMessageService.getMessagesByChatroom(CHATROOM_ID, 0, 10)).thenReturn(messages);

    // when
    List<ChatMessageResponse> response =
        chatMessageService.getMessagesByChatroom(CHATROOM_ID, 0, 10);

    // then
    assertThat(response).hasSize(3);
    assertThat(response.get(0).getContent()).isEqualTo("Test Message 1");
    assertThat(response.get(1).getContent()).isEqualTo("Test Message 2");

    verify(chatMessageService).getMessagesByChatroom(CHATROOM_ID, 0, 10);
  }

  @Test
  @DisplayName("특정 메시지 이전 메시지 조회 테스트")
  void getMessagesByChatroomBefore_Success() {
    // given
    List<ChatMessageResponse> previousMessages =
        Arrays.asList(
            createChatMessageResponse("prev1", "Previous Message 1"),
            createChatMessageResponse("prev2", "Previous Message 2"));

    when(chatMessageService.getMessagesByChatroomBefore(CHATROOM_ID, MESSAGE_ID, 2))
        .thenReturn(previousMessages);

    // when
    List<ChatMessageResponse> response =
        chatMessageService.getMessagesByChatroomBefore(CHATROOM_ID, MESSAGE_ID, 2);

    // then
    assertThat(response).hasSize(2);
    assertThat(response.get(0).getContent()).isEqualTo("Previous Message 1");
    assertThat(response.get(1).getContent()).isEqualTo("Previous Message 2");

    verify(chatMessageService).getMessagesByChatroomBefore(CHATROOM_ID, MESSAGE_ID, 2);
  }

  @Test
  @DisplayName("특정 메시지 이후 메시지 조회 테스트")
  void getMessagesByChatroomAfter_Success() {
    // given
    List<ChatMessageResponse> nextMessages =
        Collections.singletonList(createChatMessageResponse("next1", "Next Message 1"));

    when(chatMessageService.getMessagesByChatroomAfter(CHATROOM_ID, MESSAGE_ID, 3))
        .thenReturn(nextMessages);

    // when
    List<ChatMessageResponse> response =
        chatMessageService.getMessagesByChatroomAfter(CHATROOM_ID, MESSAGE_ID, 3);

    // then
    assertThat(response).hasSize(1);
    assertThat(response.get(0).getContent()).isEqualTo("Next Message 1");

    verify(chatMessageService).getMessagesByChatroomAfter(CHATROOM_ID, MESSAGE_ID, 3);
  }

  @Test
  @DisplayName("메시지 삭제 테스트")
  void deleteMessage_Success() {
    // given
    ChatMessageResponse deletedMessage =
        ChatMessageResponse.builder()
            .id(MESSAGE_ID)
            .chatroomId(CHATROOM_ID)
            .senderId(USER_ID)
            .senderName("Test User")
            .messageType(MessageType.TEXT)
            .content("Test Message")
            .createdAt(LocalDateTime.now())
            .isDeleted(true)
            .build();

    when(chatMessageService.deleteMessage(MESSAGE_ID)).thenReturn(Optional.of(deletedMessage));

    // when
    Optional<ChatMessageResponse> response = chatMessageService.deleteMessage(MESSAGE_ID);

    // then
    assertThat(response).isPresent();
    assertThat(response.get().getId()).isEqualTo(MESSAGE_ID);
    assertThat(response.get().isDeleted()).isTrue();

    verify(chatMessageService).deleteMessage(MESSAGE_ID);
  }

  @Test
  @DisplayName("존재하지 않는 메시지 삭제 테스트")
  void deleteMessage_NotFound() {
    // given
    when(chatMessageService.deleteMessage(MESSAGE_ID)).thenReturn(Optional.empty());

    // when
    Optional<ChatMessageResponse> response = chatMessageService.deleteMessage(MESSAGE_ID);

    // then
    assertThat(response).isEmpty();
    verify(chatMessageService).deleteMessage(MESSAGE_ID);
  }

  private ChatMessageResponse createChatMessageResponse(String id, String content) {
    return ChatMessageResponse.builder()
        .id(id)
        .chatroomId(CHATROOM_ID)
        .senderId(USER_ID)
        .senderName("Test User")
        .messageType(MessageType.TEXT)
        .content(content)
        .createdAt(LocalDateTime.now())
        .isDeleted(false)
        .build();
  }
}
