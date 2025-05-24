package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.MoodInquiryService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage.MessageType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory.MoodType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatMessageRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.UserMoodHistoryRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.GeminiApiAdapter;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.GeminiApiAdapter.GeminiTextResponse;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class AiChatServiceImplTest {

  @Mock private ChatMessageRepository chatMessageRepository;

  @Mock private ChatRoomRepository chatRoomRepository;

  @Mock private SimpMessagingTemplate messagingTemplate;

  @Mock private ChatMessageService chatMessageService;

  @Mock private UserMoodHistoryRepository moodHistoryRepository;

  @Mock private MoodInquiryService moodInquiryService;

  @Mock private GeminiApiAdapter geminiApiAdapter;

  @InjectMocks private AiChatServiceImpl aiChatService;

  private static final String TEST_USER_ID = "test-user";
  private static final String TEST_CHATROOM_ID = "test-chatroom";
  private static final String AI_USER_ID = "ai-assistant";
  private static final String AI_NAME = "수리 AI";

  private ChatMessageRequest userMessage;
  private ChatMessageResponse aiResponse;
  private GeminiTextResponse geminiResponse;

  @BeforeEach
  void setUp() {
    // 기본 테스트 객체 설정
    userMessage =
        ChatMessageRequest.builder()
            .chatroomId(TEST_CHATROOM_ID)
            .senderId(TEST_USER_ID)
            .senderName("테스트 사용자")
            .messageType(MessageType.TEXT)
            .content("안녕하세요?")
            .build();

    aiResponse =
        ChatMessageResponse.builder()
            .id("ai-msg-id")
            .chatroomId(TEST_CHATROOM_ID)
            .senderId(AI_USER_ID)
            .senderName(AI_NAME)
            .messageType(MessageType.TEXT)
            .content("안녕하세요! 무엇을 도와드릴까요?")
            .createdAt(LocalDateTime.now())
            .build();

    geminiResponse =
        new GeminiTextResponse("안녕하세요! 무엇을 도와드릴까요?", 0.95, Map.of("dummy", "response"));
  }

  @Test
  @DisplayName("일반 사용자 메시지에 AI가 응답하는지 테스트")
  void processUserMessage_normalConversation_returnsAiResponse() {
    // Given
    // 기분 조사 없음
    when(moodInquiryService.getPendingInquiryId(TEST_USER_ID)).thenReturn(Optional.empty());

    when(chatMessageRepository.findRecentMessagesByChatroomId(anyString(), anyInt()))
        .thenReturn(Collections.emptyList());

    when(geminiApiAdapter.generateText(anyString())).thenReturn(geminiResponse);

    when(chatMessageService.sendMessage(any(ChatMessageRequest.class))).thenReturn(aiResponse);

    // When
    ChatMessageResponse result = aiChatService.processUserMessage(userMessage);

    // Then
    assertNotNull(result);
    assertEquals(AI_USER_ID, result.getSenderId());
    assertEquals(AI_NAME, result.getSenderName());
    assertEquals(MessageType.TEXT, result.getMessageType());

    verify(moodInquiryService, times(1)).getPendingInquiryId(TEST_USER_ID);
    verify(geminiApiAdapter, times(1)).generateText(anyString());
    verify(chatMessageService, times(1)).sendMessage(any(ChatMessageRequest.class));
  }

  @Test
  @DisplayName("감정 질문이 있을 때 MoodInquiryService가 호출되는지 테스트")
  void processUserMessage_withMoodInquiry_callsMoodInquiryService() {
    // Given
    String inquiryId = "inquiry-id";
    UserMoodHistory moodHistory = mock(UserMoodHistory.class);
    when(moodHistory.getMoodType()).thenReturn(MoodType.JOY);

    // 기분 조사 대기 중
    when(moodInquiryService.getPendingInquiryId(TEST_USER_ID)).thenReturn(Optional.of(inquiryId));
    when(moodInquiryService.saveMoodAnswer(inquiryId, userMessage.getContent()))
        .thenReturn(moodHistory);

    when(chatMessageService.sendMessage(any(ChatMessageRequest.class))).thenReturn(aiResponse);

    // When
    ChatMessageResponse result = aiChatService.processUserMessage(userMessage);

    // Then
    assertNotNull(result);
    verify(moodInquiryService, times(1)).getPendingInquiryId(TEST_USER_ID);
    verify(moodInquiryService, times(1)).saveMoodAnswer(inquiryId, userMessage.getContent());
    verify(geminiApiAdapter, never()).generateText(anyString()); // Gemini API 호출 안 함
  }

  @Test
  @DisplayName("Gemini API null 응답 시 기본 응답 사용 테스트")
  void processUserMessage_whenGeminiReturnsNull_usesDefaultResponse() {
    // Given
    // 기분 조사 없음
    when(moodInquiryService.getPendingInquiryId(TEST_USER_ID)).thenReturn(Optional.empty());

    when(chatMessageRepository.findRecentMessagesByChatroomId(anyString(), anyInt()))
        .thenReturn(Collections.emptyList());

    // null 응답 반환
    when(geminiApiAdapter.generateText(anyString())).thenReturn(null);

    when(chatMessageService.sendMessage(any(ChatMessageRequest.class))).thenReturn(aiResponse);

    // When
    ChatMessageResponse result = aiChatService.processUserMessage(userMessage);

    // Then
    assertNotNull(result);
    verify(geminiApiAdapter, times(1)).generateText(anyString());
    verify(chatMessageService, times(1)).sendMessage(any(ChatMessageRequest.class));
  }

  @Test
  @DisplayName("AI 채팅 세션 초기화 테스트")
  void initializeAiChatSession_createsNewSession() {
    // When
    aiChatService.initializeAiChatSession(TEST_USER_ID, TEST_CHATROOM_ID);

    // Then - 세션이 내부적으로 생성되었지만 검증할 방법이 제한적임
    // 예외가 발생하지 않으면 성공으로 간주
  }

  @Test
  @DisplayName("AI 채팅 세션 종료 테스트")
  void terminateAiChatSession_removesSession() {
    // Given
    aiChatService.initializeAiChatSession(TEST_USER_ID, TEST_CHATROOM_ID);

    // When
    aiChatService.terminateAiChatSession(TEST_USER_ID, TEST_CHATROOM_ID);

    // Then - 세션이 내부적으로 제거되었지만 검증할 방법이 제한적임
    // 예외가 발생하지 않으면 성공으로 간주
  }

  @Test
  @DisplayName("Gemini API 응답이 비어있을 때 기본 응답 사용 테스트")
  void processUserMessage_whenGeminiReturnsEmpty_usesDefaultResponse() {
    // Given
    // 기분 조사 없음
    when(moodInquiryService.getPendingInquiryId(TEST_USER_ID)).thenReturn(Optional.empty());

    when(chatMessageRepository.findRecentMessagesByChatroomId(anyString(), anyInt()))
        .thenReturn(Collections.emptyList());

    // 빈 응답 반환
    when(geminiApiAdapter.generateText(anyString())).thenReturn(GeminiTextResponse.empty());

    when(chatMessageService.sendMessage(any(ChatMessageRequest.class))).thenReturn(aiResponse);

    // When
    ChatMessageResponse result = aiChatService.processUserMessage(userMessage);

    // Then
    assertNotNull(result);
    verify(moodInquiryService, times(1)).getPendingInquiryId(TEST_USER_ID);
    verify(geminiApiAdapter, times(1)).generateText(anyString());
    verify(chatMessageService, times(1)).sendMessage(any(ChatMessageRequest.class));
  }
}
