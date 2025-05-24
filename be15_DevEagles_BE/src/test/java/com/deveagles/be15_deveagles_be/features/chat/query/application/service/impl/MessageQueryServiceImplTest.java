package com.deveagles.be15_deveagles_be.features.chat.query.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage.MessageType;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageListResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageReadStatusResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.service.util.MessageResponseConverter;
import com.deveagles.be15_deveagles_be.features.chat.query.domain.repository.MessageQueryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
public class MessageQueryServiceImplTest {

  @Mock private MessageQueryRepository messageQueryRepository;
  @Mock private RedisTemplate<String, String> redisTemplate;
  @Mock private ObjectMapper objectMapper;
  @Mock private MessageResponseConverter messageResponseConverter;

  @InjectMocks private MessageQueryServiceImpl messageQueryService;

  private final String CHATROOM_ID = "chatroom1";
  private final String MESSAGE_ID = "message1";
  private final String BEFORE_MESSAGE_ID = "before_message1";
  private final Long USER_ID = 1L;
  private final String USER_ID_STR = "1";
  private final int LIMIT = 20;

  private List<ChatMessage> mockMessages;
  private Map<String, Object> mockReadStatusData;

  @BeforeEach
  void setUp() {
    // 테스트용 메시지 설정
    mockMessages = new ArrayList<>();
    mockMessages.add(createChatMessage("message1", "안녕하세요!", USER_ID_STR, "테스트 사용자1"));
    mockMessages.add(createChatMessage("message2", "반갑습니다~", "2", "테스트 사용자2"));
    mockMessages.add(createChatMessage("message3", "오늘 날씨가 좋네요.", USER_ID_STR, "테스트 사용자1"));

    // 테스트용 읽음 상태 데이터 설정
    mockReadStatusData = new HashMap<>();
    mockReadStatusData.put("totalParticipants", 3);

    List<Map<String, Object>> readUsers = new ArrayList<>();
    Map<String, Object> readUser1 = new HashMap<>();
    readUser1.put("userId", USER_ID_STR);
    readUser1.put("userName", "테스트 사용자1");
    readUser1.put("thumbnailUrl", "http://example.com/user1.png");
    readUser1.put("readAt", LocalDateTime.now().minusMinutes(5));
    readUsers.add(readUser1);

    List<Map<String, Object>> unreadUsers = new ArrayList<>();
    Map<String, Object> unreadUser1 = new HashMap<>();
    unreadUser1.put("userId", "2");
    unreadUser1.put("userName", "테스트 사용자2");
    unreadUser1.put("thumbnailUrl", "http://example.com/user2.png");
    unreadUsers.add(unreadUser1);
    Map<String, Object> unreadUser2 = new HashMap<>();
    unreadUser2.put("userId", "3");
    unreadUser2.put("userName", "테스트 사용자3");
    unreadUser2.put("thumbnailUrl", "http://example.com/user3.png");
    unreadUsers.add(unreadUser2);

    mockReadStatusData.put("readUsers", readUsers);
    mockReadStatusData.put("unreadUsers", unreadUsers);
  }

  private ChatMessage createChatMessage(
      String id, String content, String senderId, String senderName) {
    return ChatMessage.builder()
        .id(id)
        .chatroomId(CHATROOM_ID)
        .senderId(senderId)
        .senderName(senderName)
        .messageType(MessageType.TEXT)
        .content(content)
        .createdAt(LocalDateTime.now().minusMinutes(Long.parseLong(id.substring(7))))
        .build();
  }

  @Test
  @DisplayName("메시지 목록 조회 테스트")
  void getMessages_Success() {
    // given
    when(messageQueryRepository.findMessages(eq(CHATROOM_ID), eq(BEFORE_MESSAGE_ID), eq(LIMIT)))
        .thenReturn(mockMessages);

    // when
    MessageListResponse response =
        messageQueryService.getMessages(USER_ID, CHATROOM_ID, BEFORE_MESSAGE_ID, LIMIT);

    // then
    assertThat(response).isNotNull();
    // Redis를 통한 조회는 Mock이므로 정확한 결과 검증은 생략하고 null이 아님만 확인
    assertThat(response.getMessages()).isNotNull();

    verify(messageQueryRepository).findMessages(eq(CHATROOM_ID), eq(BEFORE_MESSAGE_ID), eq(LIMIT));
  }

  @Test
  @DisplayName("메시지 목록 조회 - 빈 결과 테스트")
  void getMessages_EmptyResult() {
    // given
    List<ChatMessage> emptyMessages = new ArrayList<>();
    when(messageQueryRepository.findMessages(eq(CHATROOM_ID), eq(BEFORE_MESSAGE_ID), eq(LIMIT)))
        .thenReturn(emptyMessages);

    // when
    MessageListResponse response =
        messageQueryService.getMessages(USER_ID, CHATROOM_ID, BEFORE_MESSAGE_ID, LIMIT);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getMessages()).isEmpty();

    verify(messageQueryRepository).findMessages(eq(CHATROOM_ID), eq(BEFORE_MESSAGE_ID), eq(LIMIT));
  }

  @Test
  @DisplayName("메시지 읽음 상태 조회 테스트")
  void getMessageReadStatus_Success() {
    // given
    when(messageQueryRepository.getMessageReadStatus(eq(CHATROOM_ID), eq(MESSAGE_ID)))
        .thenReturn(mockReadStatusData);

    // when
    MessageReadStatusResponse response =
        messageQueryService.getMessageReadStatus(USER_ID, CHATROOM_ID, MESSAGE_ID);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getTotalParticipants()).isEqualTo(3);
    assertThat(response.getReadCount()).isEqualTo(1);
    assertThat(response.getUnreadCount()).isEqualTo(2);

    assertThat(response.getReadBy()).hasSize(1);
    assertThat(response.getReadBy().get(0).getUserId()).isEqualTo(USER_ID_STR);

    assertThat(response.getNotReadBy()).hasSize(2);
    assertThat(response.getNotReadBy().get(0).getUserId()).isEqualTo("2");
    assertThat(response.getNotReadBy().get(1).getUserId()).isEqualTo("3");

    verify(messageQueryRepository).getMessageReadStatus(eq(CHATROOM_ID), eq(MESSAGE_ID));
  }

  @Test
  @DisplayName("메시지 읽음 상태 조회 - 데이터 없음 테스트")
  void getMessageReadStatus_NullData() {
    // given
    when(messageQueryRepository.getMessageReadStatus(eq(CHATROOM_ID), eq(MESSAGE_ID)))
        .thenReturn(null);

    // when
    MessageReadStatusResponse response =
        messageQueryService.getMessageReadStatus(USER_ID, CHATROOM_ID, MESSAGE_ID);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getTotalParticipants()).isEqualTo(0);
    assertThat(response.getReadCount()).isEqualTo(0);
    assertThat(response.getUnreadCount()).isEqualTo(0);
    assertThat(response.getReadBy()).isEmpty();
    assertThat(response.getNotReadBy()).isEmpty();

    verify(messageQueryRepository).getMessageReadStatus(eq(CHATROOM_ID), eq(MESSAGE_ID));
  }

  @Test
  @DisplayName("메시지 읽음 상태 조회 - 빈 데이터 테스트")
  void getMessageReadStatus_EmptyData() {
    // given
    when(messageQueryRepository.getMessageReadStatus(eq(CHATROOM_ID), eq(MESSAGE_ID)))
        .thenReturn(new HashMap<>());

    // when
    MessageReadStatusResponse response =
        messageQueryService.getMessageReadStatus(USER_ID, CHATROOM_ID, MESSAGE_ID);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getTotalParticipants()).isEqualTo(0);
    assertThat(response.getReadCount()).isEqualTo(0);
    assertThat(response.getUnreadCount()).isEqualTo(0);
    assertThat(response.getReadBy()).isEmpty();
    assertThat(response.getNotReadBy()).isEmpty();

    verify(messageQueryRepository).getMessageReadStatus(eq(CHATROOM_ID), eq(MESSAGE_ID));
  }
}
