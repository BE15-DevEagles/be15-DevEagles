package com.deveagles.be15_deveagles_be.features.chat.query.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.LastMessageInfo;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.Participant;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomListResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomReadSummaryResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.service.util.ChatroomResponseConverter;
import com.deveagles.be15_deveagles_be.features.chat.query.domain.repository.ChatroomQueryRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChatroomQueryServiceImplTest {

  @Mock private ChatroomQueryRepository chatroomQueryRepository;
  @Mock private ChatroomResponseConverter chatroomResponseConverter;

  @InjectMocks private ChatroomQueryServiceImpl chatroomQueryService;

  private final String TEAM_ID = "team1";
  private final String CHATROOM_ID = "chatroom1";
  private final Long USER_ID = 1L;
  private final String USER_ID_STR = "1";

  private ChatRoom mockChatRoom;
  private List<Participant> mockParticipants;
  private LastMessageInfo mockLastMessage;

  @BeforeEach
  void setUp() {
    // 테스트용 참여자 설정
    mockParticipants = new ArrayList<>();
    mockParticipants.add(
        Participant.builder()
            .userId(USER_ID_STR)
            .lastReadMessageId("last_read_message_1")
            .notificationEnabled(true)
            .createdAt(LocalDateTime.now().minusDays(5))
            .build());
    mockParticipants.add(
        Participant.builder()
            .userId("2")
            .lastReadMessageId(null)
            .notificationEnabled(true)
            .createdAt(LocalDateTime.now().minusDays(3))
            .build());

    // 테스트용 마지막 메시지 설정
    mockLastMessage =
        LastMessageInfo.builder()
            .id("last_message_id")
            .content("안녕하세요!")
            .senderId(USER_ID_STR)
            .senderName("테스트 사용자")
            .sentAt(LocalDateTime.now().minusHours(1))
            .build();

    // 테스트용 채팅방 설정
    mockChatRoom =
        ChatRoom.builder()
            .id(CHATROOM_ID)
            .teamId(TEAM_ID)
            .name("테스트 채팅방")
            .type(ChatRoomType.GROUP)
            .isDefault(false)
            .participants(mockParticipants)
            .lastMessage(mockLastMessage)
            .createdAt(LocalDateTime.now().minusDays(7))
            .build();
  }

  @Test
  @DisplayName("채팅방 목록 조회 테스트")
  void getChatrooms_Success() {
    // given
    List<ChatRoom> chatRooms = Arrays.asList(mockChatRoom);
    int page = 0;
    int size = 10;

    ChatroomResponse mockChatroomResponse =
        ChatroomResponse.builder()
            .id(CHATROOM_ID)
            .teamId(TEAM_ID)
            .name("테스트 채팅방")
            .isDefault(false)
            .type(ChatRoomType.GROUP.name())
            .participants(new ArrayList<>())
            .createdAt(LocalDateTime.now().minusDays(7))
            .isDeleted(false)
            .build();

    when(chatroomQueryRepository.findChatroomsByUserIdAndTeamId(
            eq(USER_ID), eq(TEAM_ID), eq(page), eq(size)))
        .thenReturn(chatRooms);
    when(chatroomQueryRepository.countChatroomsByUserIdAndTeamId(eq(USER_ID), eq(TEAM_ID)))
        .thenReturn(1);
    when(chatroomResponseConverter.convertToChatroomResponse(mockChatRoom))
        .thenReturn(mockChatroomResponse);

    // when
    ChatroomListResponse response = chatroomQueryService.getChatrooms(USER_ID, TEAM_ID, page, size);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getTotalCount()).isEqualTo(1);
    assertThat(response.getChatrooms()).hasSize(1);
    assertThat(response.getChatrooms().get(0).getId()).isEqualTo(CHATROOM_ID);
    assertThat(response.getChatrooms().get(0).getTeamId()).isEqualTo(TEAM_ID);

    verify(chatroomQueryRepository)
        .findChatroomsByUserIdAndTeamId(eq(USER_ID), eq(TEAM_ID), eq(page), eq(size));
    verify(chatroomQueryRepository).countChatroomsByUserIdAndTeamId(eq(USER_ID), eq(TEAM_ID));
    verify(chatroomResponseConverter).convertToChatroomResponse(mockChatRoom);
  }

  @Test
  @DisplayName("채팅방 목록 조회 - 빈 결과 테스트")
  void getChatrooms_EmptyResult() {
    // given
    List<ChatRoom> chatRooms = new ArrayList<>();
    int page = 0;
    int size = 10;

    when(chatroomQueryRepository.findChatroomsByUserIdAndTeamId(
            eq(USER_ID), eq(TEAM_ID), eq(page), eq(size)))
        .thenReturn(chatRooms);
    when(chatroomQueryRepository.countChatroomsByUserIdAndTeamId(eq(USER_ID), eq(TEAM_ID)))
        .thenReturn(0);

    // when
    ChatroomListResponse response = chatroomQueryService.getChatrooms(USER_ID, TEAM_ID, page, size);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getTotalCount()).isEqualTo(0);
    assertThat(response.getChatrooms()).isEmpty();

    verify(chatroomQueryRepository)
        .findChatroomsByUserIdAndTeamId(eq(USER_ID), eq(TEAM_ID), eq(page), eq(size));
    verify(chatroomQueryRepository).countChatroomsByUserIdAndTeamId(eq(USER_ID), eq(TEAM_ID));
  }

  @Test
  @DisplayName("채팅방 상세 조회 테스트")
  void getChatroom_Success() {
    // given
    ChatroomResponse mockChatroomResponse =
        ChatroomResponse.builder()
            .id(CHATROOM_ID)
            .teamId(TEAM_ID)
            .name("테스트 채팅방")
            .isDefault(false)
            .type(ChatRoomType.GROUP.name())
            .lastMessage(
                ChatroomResponse.LastMessageDto.builder()
                    .id(mockLastMessage.getId())
                    .content(mockLastMessage.getContent())
                    .senderId(mockLastMessage.getSenderId())
                    .senderName(mockLastMessage.getSenderName())
                    .sentAt(mockLastMessage.getSentAt())
                    .build())
            .participants(new ArrayList<>())
            .createdAt(LocalDateTime.now().minusDays(7))
            .isDeleted(false)
            .build();

    when(chatroomQueryRepository.findChatroomById(eq(CHATROOM_ID)))
        .thenReturn(Optional.of(mockChatRoom));
    when(chatroomResponseConverter.convertToChatroomResponse(mockChatRoom))
        .thenReturn(mockChatroomResponse);

    // when
    ChatroomResponse response = chatroomQueryService.getChatroom(USER_ID, CHATROOM_ID);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(CHATROOM_ID);
    assertThat(response.getTeamId()).isEqualTo(TEAM_ID);
    assertThat(response.getType()).isEqualTo(ChatRoomType.GROUP.name());
    assertThat(response.getLastMessage()).isNotNull();
    assertThat(response.getLastMessage().getId()).isEqualTo(mockLastMessage.getId());

    verify(chatroomQueryRepository).findChatroomById(eq(CHATROOM_ID));
    verify(chatroomResponseConverter).convertToChatroomResponse(mockChatRoom);
  }

  @Test
  @DisplayName("존재하지 않는 채팅방 조회 테스트")
  void getChatroom_NotFound() {
    // given
    when(chatroomQueryRepository.findChatroomById(anyString())).thenReturn(Optional.empty());

    // when
    ChatroomResponse response = chatroomQueryService.getChatroom(USER_ID, CHATROOM_ID);

    // then
    assertThat(response).isNull();

    verify(chatroomQueryRepository).findChatroomById(eq(CHATROOM_ID));
  }

  @Test
  @DisplayName("채팅방 읽음 상태 요약 조회 테스트")
  void getChatroomReadSummary_Success() {
    // given
    when(chatroomQueryRepository.findChatroomById(eq(CHATROOM_ID)))
        .thenReturn(Optional.of(mockChatRoom));

    // when
    ChatroomReadSummaryResponse response =
        chatroomQueryService.getChatroomReadSummary(USER_ID, CHATROOM_ID);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getLastMessageId()).isEqualTo(mockLastMessage.getId());
    assertThat(response.getLastMessageTime()).isEqualTo(mockLastMessage.getSentAt());
    assertThat(response.getTotalParticipants()).isEqualTo(2);
    assertThat(response.getUnreadByUsers()).hasSize(2); // 읽지 않은 사용자가 2명
    assertThat(response.getReadCount()).isEqualTo(0); // 읽은 사용자는 0명

    verify(chatroomQueryRepository).findChatroomById(eq(CHATROOM_ID));
  }

  @Test
  @DisplayName("존재하지 않는 채팅방 읽음 상태 요약 조회 테스트")
  void getChatroomReadSummary_NotFound() {
    // given
    when(chatroomQueryRepository.findChatroomById(anyString())).thenReturn(Optional.empty());

    // when
    ChatroomReadSummaryResponse response =
        chatroomQueryService.getChatroomReadSummary(USER_ID, CHATROOM_ID);

    // then
    assertThat(response).isNull();

    verify(chatroomQueryRepository).findChatroomById(eq(CHATROOM_ID));
  }
}
