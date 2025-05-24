package com.deveagles.be15_deveagles_be.features.chat.command.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatRoomResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl.ChatRoomServiceImpl;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.util.ChatRoomHelper;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.util.ChatRoomValidator;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.util.ParticipantValidator;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChatRoomParticipantTest {

  @Mock private ChatRoomRepository chatRoomRepository;
  @Mock private ChatRoomValidator chatRoomValidator;
  @Mock private ChatRoomHelper chatRoomHelper;
  @Mock private ParticipantValidator participantValidator;

  @InjectMocks private ChatRoomServiceImpl chatRoomService;

  private ChatRoom testChatRoom;
  private final String chatroomId = "chat123";
  private final String testUserId1 = "user1";
  private final String testUserId2 = "user2";

  @BeforeEach
  void setUp() {
    testChatRoom =
        ChatRoom.builder()
            .id(chatroomId)
            .teamId("team1")
            .name("Test Chat")
            .type(ChatRoom.ChatRoomType.GROUP)
            .isDefault(false)
            .createdAt(LocalDateTime.now())
            .participants(new ArrayList<>())
            .build();

    testChatRoom.addParticipant(testUserId1);
  }

  @Test
  @DisplayName("새로운 참가자 추가 성공 테스트")
  void addParticipantSuccess() {
    // given
    String newUserId = testUserId2;
    ChatRoomResponse mockResponse = createMockResponse();

    when(chatRoomValidator.validateAndGetChatRoom(chatroomId)).thenReturn(testChatRoom);
    doNothing().when(participantValidator).validateParticipantNotExists(testChatRoom, newUserId);
    when(chatRoomHelper.executeAndSave(eq(testChatRoom), any(), any(ChatErrorCode.class)))
        .thenReturn(mockResponse);

    // when
    ChatRoomResponse response = chatRoomService.addParticipantToChatRoom(chatroomId, newUserId);

    // then
    assertNotNull(response);
    verify(chatRoomValidator).validateAndGetChatRoom(chatroomId);
    verify(participantValidator).validateParticipantNotExists(testChatRoom, newUserId);
    verify(chatRoomHelper).executeAndSave(eq(testChatRoom), any(), any(ChatErrorCode.class));
  }

  @Test
  @DisplayName("이미 존재하는 참가자 추가 시 예외 발생 테스트")
  void addExistingParticipantFailure() {
    // given
    String existingUserId = testUserId1;
    when(chatRoomValidator.validateAndGetChatRoom(chatroomId)).thenReturn(testChatRoom);
    doThrow(new ChatBusinessException(ChatErrorCode.PARTICIPANT_ALREADY_EXISTS))
        .when(participantValidator)
        .validateParticipantNotExists(testChatRoom, existingUserId);

    // when & then
    ChatBusinessException exception =
        assertThrows(
            ChatBusinessException.class,
            () -> chatRoomService.addParticipantToChatRoom(chatroomId, existingUserId));

    assertEquals(ChatErrorCode.PARTICIPANT_ALREADY_EXISTS, exception.getErrorCode());
    verify(chatRoomValidator).validateAndGetChatRoom(chatroomId);
    verify(participantValidator).validateParticipantNotExists(testChatRoom, existingUserId);
  }

  @Test
  @DisplayName("참가자 제거 성공 테스트")
  void removeParticipantSuccess() {
    // given
    String userId = testUserId1;
    ChatRoom.Participant participant = testChatRoom.getParticipant(userId);
    ChatRoomResponse mockResponse = createMockResponse();

    when(chatRoomValidator.validateAndGetChatRoom(chatroomId)).thenReturn(testChatRoom);
    when(participantValidator.validateAndGetParticipant(testChatRoom, userId))
        .thenReturn(participant);
    when(chatRoomHelper.executeAndSave(eq(testChatRoom), any(), any(ChatErrorCode.class)))
        .thenReturn(mockResponse);

    // when
    ChatRoomResponse response = chatRoomService.removeParticipantFromChatRoom(chatroomId, userId);

    // then
    assertNotNull(response);
    verify(chatRoomValidator).validateAndGetChatRoom(chatroomId);
    verify(participantValidator).validateAndGetParticipant(testChatRoom, userId);
    verify(chatRoomHelper).executeAndSave(eq(testChatRoom), any(), any(ChatErrorCode.class));
  }

  @Test
  @DisplayName("존재하지 않는 참가자 제거 시 예외 발생 테스트")
  void removeNonExistingParticipantFailure() {
    // given
    String nonExistingUserId = "nonExistingUser";
    when(chatRoomValidator.validateAndGetChatRoom(chatroomId)).thenReturn(testChatRoom);
    doThrow(new ChatBusinessException(ChatErrorCode.PARTICIPANT_NOT_FOUND))
        .when(participantValidator)
        .validateAndGetParticipant(testChatRoom, nonExistingUserId);

    // when & then
    ChatBusinessException exception =
        assertThrows(
            ChatBusinessException.class,
            () -> chatRoomService.removeParticipantFromChatRoom(chatroomId, nonExistingUserId));

    assertEquals(ChatErrorCode.PARTICIPANT_NOT_FOUND, exception.getErrorCode());
    verify(chatRoomValidator).validateAndGetChatRoom(chatroomId);
    verify(participantValidator).validateAndGetParticipant(testChatRoom, nonExistingUserId);
  }

  @Test
  @DisplayName("참가자 알림 설정 토글 성공 테스트")
  void toggleParticipantNotificationSuccess() {
    // given
    String userId = testUserId1;
    ChatRoom.Participant participant = testChatRoom.getParticipant(userId);
    ChatRoomResponse mockResponse = createMockResponse();

    when(chatRoomValidator.validateAndGetChatRoom(chatroomId)).thenReturn(testChatRoom);
    when(participantValidator.validateAndGetParticipant(testChatRoom, userId))
        .thenReturn(participant);
    when(chatRoomHelper.executeParticipantOperationAndSave(
            eq(testChatRoom), eq(participant), any(), any(ChatErrorCode.class)))
        .thenReturn(mockResponse);

    // when
    ChatRoomResponse response = chatRoomService.toggleParticipantNotification(chatroomId, userId);

    // then
    assertNotNull(response);
    verify(chatRoomValidator).validateAndGetChatRoom(chatroomId);
    verify(participantValidator).validateAndGetParticipant(testChatRoom, userId);
    verify(chatRoomHelper)
        .executeParticipantOperationAndSave(
            eq(testChatRoom), eq(participant), any(), any(ChatErrorCode.class));
  }

  @Test
  @DisplayName("존재하지 않는 채팅방에 참가자 추가 시 예외 발생 테스트")
  void addParticipantToChatRoomNotFound() {
    // given
    String nonExistingChatroomId = "nonExistingChatroom";
    String userId = testUserId1;

    when(chatRoomValidator.validateAndGetChatRoom(nonExistingChatroomId))
        .thenThrow(new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    // when & then
    ChatBusinessException exception =
        assertThrows(
            ChatBusinessException.class,
            () -> chatRoomService.addParticipantToChatRoom(nonExistingChatroomId, userId));

    assertEquals(ChatErrorCode.CHAT_ROOM_NOT_FOUND, exception.getErrorCode());
    verify(chatRoomValidator).validateAndGetChatRoom(nonExistingChatroomId);
  }

  @Test
  @DisplayName("삭제된 채팅방에 참가자 추가 시 예외 발생 테스트")
  void addParticipantToDeletedChatRoom() {
    // given
    String deletedChatroomId = "deletedChatroom";
    String userId = testUserId1;

    when(chatRoomValidator.validateAndGetChatRoom(deletedChatroomId))
        .thenThrow(new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

    // when & then
    ChatBusinessException exception =
        assertThrows(
            ChatBusinessException.class,
            () -> chatRoomService.addParticipantToChatRoom(deletedChatroomId, userId));

    assertEquals(ChatErrorCode.CHAT_ROOM_NOT_FOUND, exception.getErrorCode());
    verify(chatRoomValidator).validateAndGetChatRoom(deletedChatroomId);
  }

  private ChatRoomResponse createMockResponse() {
    return ChatRoomResponse.builder()
        .id(chatroomId)
        .teamId("team1")
        .name("Test Chat")
        .isDefault(false)
        .type(ChatRoom.ChatRoomType.GROUP)
        .participants(new ArrayList<>())
        .createdAt(LocalDateTime.now())
        .isDeleted(false)
        .build();
  }
}
