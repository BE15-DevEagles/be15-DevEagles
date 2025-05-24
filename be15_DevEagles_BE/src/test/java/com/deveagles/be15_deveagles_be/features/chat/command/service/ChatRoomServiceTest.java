package com.deveagles.be15_deveagles_be.features.chat.command.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.CreateChatRoomRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl.ChatRoomServiceImpl;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.util.ChatRoomHelper;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.util.ChatRoomOperationHelper;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.util.ChatRoomValidator;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.util.ParticipantValidator;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** ChatRoomService 예외 처리 테스트 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatRoomServiceTest {

  @Mock private ChatRoomRepository chatRoomRepository;
  @Mock private ChatRoomValidator chatRoomValidator;
  @Mock private ParticipantValidator participantValidator;
  @Mock private ChatRoomHelper chatRoomHelper;
  @Mock private ChatRoomOperationHelper operationHelper;

  private ChatRoomService chatRoomService;

  @BeforeEach
  void setUp() {
    chatRoomService =
        new ChatRoomServiceImpl(
            chatRoomRepository,
            chatRoomValidator,
            participantValidator,
            chatRoomHelper,
            operationHelper);
  }

  @Test
  @DisplayName("기본 채팅방이 이미 존재할 때 DEFAULT_CHATROOM_ALREADY_EXISTS 예외 발생")
  void createDefaultChatRoom_whenDefaultChatRoomAlreadyExists_thenThrowException() {
    // given
    String teamId = "team-123";
    String name = "기본 채팅방";

    // validator에서 예외가 발생하도록 설정
    doThrow(new ChatBusinessException(ChatErrorCode.DEFAULT_CHATROOM_ALREADY_EXISTS))
        .when(chatRoomValidator)
        .validateDefaultChatRoomNotExists(teamId);

    // when & then
    ChatBusinessException exception =
        assertThrows(
            ChatBusinessException.class, () -> chatRoomService.createDefaultChatRoom(teamId, name));

    assertEquals(ChatErrorCode.DEFAULT_CHATROOM_ALREADY_EXISTS.getCode(), exception.getCode());
    assertEquals(
        ChatErrorCode.DEFAULT_CHATROOM_ALREADY_EXISTS.getMessage(), exception.getOriginalMessage());
    verify(chatRoomValidator, times(1)).validateDefaultChatRoomNotExists(teamId);
    verify(chatRoomHelper, never()).saveAndConvertToResponse(any(ChatRoom.class));
  }

  @Test
  @DisplayName("기본 채팅방 삭제 시도 시 DEFAULT_CHATROOM_CANNOT_BE_DELETED 예외 발생")
  void deleteChatRoom_whenDefaultChatRoom_thenThrowException() {
    // given
    String chatRoomId = UUID.randomUUID().toString();

    ChatRoom defaultChatRoom = mock(ChatRoom.class);
    when(defaultChatRoom.isDefault()).thenReturn(true);

    when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(defaultChatRoom));

    // validator에서 예외가 발생하도록 설정
    doThrow(new ChatBusinessException(ChatErrorCode.DEFAULT_CHATROOM_CANNOT_BE_DELETED))
        .when(chatRoomValidator)
        .validateNotDefaultChatRoom(defaultChatRoom);

    // when & then
    ChatBusinessException exception =
        assertThrows(ChatBusinessException.class, () -> chatRoomService.deleteChatRoom(chatRoomId));

    assertEquals(ChatErrorCode.DEFAULT_CHATROOM_CANNOT_BE_DELETED.getCode(), exception.getCode());
    assertEquals(
        ChatErrorCode.DEFAULT_CHATROOM_CANNOT_BE_DELETED.getMessage(),
        exception.getOriginalMessage());
    verify(chatRoomRepository, times(1)).findById(chatRoomId);
    verify(chatRoomValidator, times(1)).validateNotDefaultChatRoom(defaultChatRoom);
    verify(defaultChatRoom, never()).delete();
    verify(chatRoomHelper, never()).saveAndConvertToResponse(any(ChatRoom.class));
  }

  @Test
  @DisplayName("일반 채팅방 생성 시 기본 채팅방이 이미 존재할 때 예외 발생")
  void createChatRoom_whenDefaultRoomExistsAndTryingToCreateAnotherDefault_thenThrowException() {
    // given
    String teamId = "team-123";

    CreateChatRoomRequest request =
        CreateChatRoomRequest.builder()
            .teamId(teamId)
            .name("새로운 기본 채팅방")
            .type(ChatRoomType.GROUP)
            .isDefault(true)
            .participantIds(new ArrayList<>())
            .build();

    // validator에서 예외가 발생하도록 설정
    doThrow(new ChatBusinessException(ChatErrorCode.DEFAULT_CHATROOM_ALREADY_EXISTS))
        .when(chatRoomValidator)
        .validateDefaultChatRoomNotExists(teamId);

    // when & then
    ChatBusinessException exception =
        assertThrows(ChatBusinessException.class, () -> chatRoomService.createChatRoom(request));

    assertEquals(ChatErrorCode.DEFAULT_CHATROOM_ALREADY_EXISTS.getCode(), exception.getCode());
    assertEquals(
        ChatErrorCode.DEFAULT_CHATROOM_ALREADY_EXISTS.getMessage(), exception.getOriginalMessage());
    verify(chatRoomValidator, times(1)).validateDefaultChatRoomNotExists(teamId);
    verify(chatRoomHelper, never()).saveAndConvertToResponse(any(ChatRoom.class));
  }
}
