package com.deveagles.be15_deveagles_be.features.chat.command.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatRoomResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl.ChatRoomServiceImpl;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
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

    // BeforeEach에서는 모킹을 제거하고 각 테스트에서 필요한 모킹을 설정
  }

  @Test
  @DisplayName("새로운 참가자 추가 성공 테스트")
  void addParticipantSuccess() {
    // given
    String newUserId = testUserId2;
    when(chatRoomRepository.findById(chatroomId)).thenReturn(Optional.of(testChatRoom));
    when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);

    // when
    ChatRoomResponse response = chatRoomService.addParticipantToChatRoom(chatroomId, newUserId);

    // then
    assertNotNull(response);
    assertTrue(response.getParticipants().stream().anyMatch(p -> p.getUserId().equals(newUserId)));

    verify(chatRoomRepository).save(any(ChatRoom.class));
  }

  @Test
  @DisplayName("이미 존재하는 참가자 추가 시 예외 발생 테스트")
  void addExistingParticipantFailure() {
    // given
    String existingUserId = testUserId1;
    when(chatRoomRepository.findById(chatroomId)).thenReturn(Optional.of(testChatRoom));

    // when & then
    ChatBusinessException exception =
        assertThrows(
            ChatBusinessException.class,
            () -> chatRoomService.addParticipantToChatRoom(chatroomId, existingUserId));

    assertEquals(ChatErrorCode.PARTICIPANT_ALREADY_EXISTS, exception.getErrorCode());
  }

  @Test
  @DisplayName("참가자 제거 성공 테스트")
  void removeParticipantSuccess() {
    // given
    String userId = testUserId1;
    when(chatRoomRepository.findById(chatroomId)).thenReturn(Optional.of(testChatRoom));
    when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);

    // when
    ChatRoomResponse response = chatRoomService.removeParticipantFromChatRoom(chatroomId, userId);

    // then
    assertNotNull(response);
    // 참가자 삭제는 soft delete이므로 목록에서 완전히 제거되지 않고 deletedAt 값만 설정됨
    // 따라서 active 참가자 목록만 확인
    assertFalse(response.getParticipants().stream().anyMatch(p -> p.getUserId().equals(userId)));

    verify(chatRoomRepository).save(any(ChatRoom.class));
  }

  @Test
  @DisplayName("존재하지 않는 참가자 제거 시 예외 발생 테스트")
  void removeNonExistingParticipantFailure() {
    // given
    String nonExistingUserId = "nonExistingUser";
    when(chatRoomRepository.findById(chatroomId)).thenReturn(Optional.of(testChatRoom));

    // when & then
    ChatBusinessException exception =
        assertThrows(
            ChatBusinessException.class,
            () -> chatRoomService.removeParticipantFromChatRoom(chatroomId, nonExistingUserId));

    assertEquals(ChatErrorCode.PARTICIPANT_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("참가자 알림 설정 토글 성공 테스트")
  void toggleParticipantNotificationSuccess() {
    // given
    String userId = testUserId1;
    boolean beforeToggle = testChatRoom.getParticipant(userId).isNotificationEnabled();
    when(chatRoomRepository.findById(chatroomId)).thenReturn(Optional.of(testChatRoom));
    when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);

    // when
    ChatRoomResponse response = chatRoomService.toggleParticipantNotification(chatroomId, userId);

    // then
    assertNotNull(response);

    // 참가자 찾기
    Optional<ChatRoomResponse.ParticipantDto> participant =
        response.getParticipants().stream().filter(p -> p.getUserId().equals(userId)).findFirst();

    assertTrue(participant.isPresent());
    // 알림 설정이 변경되었는지 확인 (이전과 반대 값이어야 함)
    assertEquals(!beforeToggle, participant.get().isNotificationEnabled());

    verify(chatRoomRepository).save(any(ChatRoom.class));
  }

  @Test
  @DisplayName("존재하지 않는 채팅방에 참가자 추가 시 예외 발생 테스트")
  void addParticipantToChatRoomNotFound() {
    // given
    String nonExistingChatroomId = "nonExistingChatroom";
    String userId = testUserId1;

    // 존재하지 않는 채팅방에 대한 mock 설정
    when(chatRoomRepository.findById(nonExistingChatroomId)).thenReturn(Optional.empty());

    // when & then
    ChatBusinessException exception =
        assertThrows(
            ChatBusinessException.class,
            () -> chatRoomService.addParticipantToChatRoom(nonExistingChatroomId, userId));

    assertEquals(ChatErrorCode.CHAT_ROOM_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("삭제된 채팅방에 참가자 추가 시 예외 발생 테스트")
  void addParticipantToDeletedChatRoom() {
    // given
    String deletedChatroomId = "deletedChatroom";
    String userId = testUserId1;

    // 삭제된 채팅방 mock 생성
    ChatRoom deletedChatRoom =
        ChatRoom.builder()
            .id(deletedChatroomId)
            .teamId("team1")
            .name("Deleted Chat")
            .type(ChatRoom.ChatRoomType.GROUP)
            .isDefault(false)
            .createdAt(LocalDateTime.now())
            .deletedAt(LocalDateTime.now()) // deletedAt 설정
            .participants(new ArrayList<>())
            .build();

    // 삭제된 채팅방에 대한 mock 설정
    when(chatRoomRepository.findById(deletedChatroomId)).thenReturn(Optional.of(deletedChatRoom));

    // when & then
    ChatBusinessException exception =
        assertThrows(
            ChatBusinessException.class,
            () -> chatRoomService.addParticipantToChatRoom(deletedChatroomId, userId));

    assertEquals(ChatErrorCode.CHAT_ROOM_NOT_FOUND, exception.getErrorCode());
  }
}
