package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.common.dto.PagedResult;
import com.deveagles.be15_deveagles_be.common.dto.Pagination;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.EmotionAnalysisService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage.MessageType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory.MoodType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.UserMoodHistoryRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.GeminiApiAdapter;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.GeminiApiAdapter.GeminiTextResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.HuggingFaceApiAdapter;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.HuggingFaceApiAdapter.EmotionAnalysisResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.HuggingFaceApiAdapter.EmotionData;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles("test")
class MoodInquiryServiceImplTest {

  @Mock private UserMoodHistoryRepository moodHistoryRepository;

  @Mock private ChatRoomRepository chatRoomRepository;

  @Mock private ChatRoomService chatRoomService;

  @Mock private ChatMessageService chatMessageService;

  @Mock private GeminiApiAdapter geminiApiAdapter;

  @Mock private HuggingFaceApiAdapter huggingFaceApiAdapter;

  @Mock private EmotionAnalysisService emotionAnalysisService;

  @InjectMocks private MoodInquiryServiceImpl moodInquiryService;

  private static final String TEST_USER_ID = "test-user";
  private static final String TEST_CHATROOM_ID = "test-chatroom";
  private static final String AI_SENDER_ID = "ai-assistant";
  private static final String AI_SENDER_NAME = "수리 AI";

  private GeminiTextResponse geminiResponse;
  private EmotionAnalysisResponse emotionResponse;
  private UserMoodHistory moodHistory;

  @BeforeEach
  void setUp() {
    // 기본 테스트 객체 설정
    geminiResponse = new GeminiTextResponse("오늘 기분이 어떠신가요?", 0.95, Map.of("dummy", "response"));

    List<EmotionData> emotions = new ArrayList<>();
    emotions.add(new EmotionData("JOY", 0.8));
    emotions.add(new EmotionData("SADNESS", 0.1));
    emotions.add(new EmotionData("NEUTRAL", 0.1));

    emotionResponse =
        new EmotionAnalysisResponse("{\"emotion_data\": \"sample\"}", emotions, "JOY", 0.8);

    moodHistory =
        UserMoodHistory.builder()
            .userId(TEST_USER_ID)
            .moodType(MoodType.NEUTRAL)
            .inquiry("오늘 기분이 어떠신가요?")
            .inquiryId(UUID.randomUUID().toString())
            .createdAt(LocalDateTime.now())
            .build();
  }

  @Test
  @DisplayName("사용자에게 기분 질문 생성 테스트")
  void generateMoodInquiry_returnsNewInquiry() {
    // Given
    when(moodHistoryRepository.findByUserIdAndCreatedAtBetween(eq(TEST_USER_ID), any(), any()))
        .thenReturn(Arrays.asList()); // 빈 리스트 반환

    when(geminiApiAdapter.generateText(anyString())).thenReturn(geminiResponse);

    when(moodHistoryRepository.save(any(UserMoodHistory.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    UserMoodHistory result = moodInquiryService.generateMoodInquiry(TEST_USER_ID);

    // Then
    assertNotNull(result);
    assertEquals(TEST_USER_ID, result.getUserId());
    assertEquals(geminiResponse.getText(), result.getInquiry());
    assertEquals(MoodType.NEUTRAL, result.getMoodType());
    assertNotNull(result.getInquiryId());

    verify(geminiApiAdapter, times(1)).generateText(anyString());
    verify(moodHistoryRepository, times(1)).save(any(UserMoodHistory.class));
  }

  @Test
  @DisplayName("같은 날 중복 기분 질문 방지 테스트")
  void generateMoodInquiry_whenAlreadyAskedToday_throwsException() {
    // Given
    List<UserMoodHistory> existingInquiries = Arrays.asList(moodHistory);
    when(moodHistoryRepository.findByUserIdAndCreatedAtBetween(eq(TEST_USER_ID), any(), any()))
        .thenReturn(existingInquiries);

    // When & Then
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> moodInquiryService.generateMoodInquiry(TEST_USER_ID));

    assertEquals("이미 오늘 기분 질문이 생성되었습니다.", exception.getMessage());
    verify(geminiApiAdapter, never()).generateText(anyString());
    verify(moodHistoryRepository, never()).save(any(UserMoodHistory.class));
  }

  @Test
  @DisplayName("기분 질문에 대한 응답 저장 테스트")
  void saveMoodAnswer_savesUserAnswer() {
    // Given
    String inquiryId = UUID.randomUUID().toString();
    String userAnswer = "오늘은 기분이 좋아요!";

    EmotionAnalysisService.EmotionAnalysisResult mockAnalysisResult =
        mock(EmotionAnalysisService.EmotionAnalysisResult.class);
    when(mockAnalysisResult.isEmpty()).thenReturn(false);
    when(mockAnalysisResult.getRawJson()).thenReturn("{\"emotion\": \"positive\"}");
    when(mockAnalysisResult.getMoodType()).thenReturn(MoodType.JOY);
    when(mockAnalysisResult.getIntensity()).thenReturn(80);

    when(moodHistoryRepository.findByInquiryId(inquiryId)).thenReturn(Optional.of(moodHistory));
    when(emotionAnalysisService.analyzeEmotion(userAnswer)).thenReturn(mockAnalysisResult);
    when(moodHistoryRepository.save(any(UserMoodHistory.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    UserMoodHistory result = moodInquiryService.saveMoodAnswer(inquiryId, userAnswer);

    // Then
    assertNotNull(result);
    assertEquals(userAnswer, result.getUserAnswer());
    assertNotNull(result.getAnsweredAt());

    verify(emotionAnalysisService, times(1)).analyzeEmotion(userAnswer);
    verify(moodHistoryRepository, times(1)).save(any(UserMoodHistory.class));
  }

  @Test
  @DisplayName("모든 사용자에게 기분 질문 전송 테스트")
  void sendMoodInquiryToAllUsers_sendsInquiries() {
    // Given
    ChatRoom chatRoom1 = mock(ChatRoom.class);
    ChatRoom chatRoom2 = mock(ChatRoom.class);

    when(chatRoom1.getUserId()).thenReturn("user1");
    when(chatRoom1.getId()).thenReturn("chatroom1");
    when(chatRoom1.getType()).thenReturn(ChatRoomType.AI);

    when(chatRoom2.getUserId()).thenReturn("user2");
    when(chatRoom2.getId()).thenReturn("chatroom2");
    when(chatRoom2.getType()).thenReturn(ChatRoomType.AI);

    List<ChatRoom> aiChatRooms = Arrays.asList(chatRoom1, chatRoom2);

    // ChatRoomRepository에서 findActiveChatRoomsByType 호출 시 페이징된 결과 반환을 모킹
    Pagination pagination = Pagination.builder().currentPage(0).totalPages(1).totalItems(2).build();
    PagedResult<ChatRoom> pagedResult = new PagedResult<>(aiChatRooms, pagination);

    when(chatRoomRepository.findActiveChatRoomsByType(eq(ChatRoomType.AI), anyInt(), anyInt()))
        .thenReturn(pagedResult);

    // 첫 번째 사용자에 대한 모킹
    when(moodHistoryRepository.findByUserIdAndCreatedAtBetween(eq("user1"), any(), any()))
        .thenReturn(Arrays.asList()); // 빈 리스트
    when(chatRoomRepository.findByTeamIdAndUserIdAndTypeAndDeletedAtIsNull(
            isNull(), eq("user1"), eq(ChatRoomType.AI)))
        .thenReturn(Optional.of(chatRoom1));

    // 두 번째 사용자에 대한 모킹
    when(moodHistoryRepository.findByUserIdAndCreatedAtBetween(eq("user2"), any(), any()))
        .thenReturn(Arrays.asList()); // 빈 리스트
    when(chatRoomRepository.findByTeamIdAndUserIdAndTypeAndDeletedAtIsNull(
            isNull(), eq("user2"), eq(ChatRoomType.AI)))
        .thenReturn(Optional.of(chatRoom2));

    // 공통 모킹
    when(geminiApiAdapter.generateText(anyString())).thenReturn(geminiResponse);

    UserMoodHistory moodHistory1 = mock(UserMoodHistory.class);
    when(moodHistory1.getInquiryId()).thenReturn("inquiry1");
    when(moodHistory1.getInquiry()).thenReturn("오늘 기분이 어떠신가요?");

    UserMoodHistory moodHistory2 = mock(UserMoodHistory.class);
    when(moodHistory2.getInquiryId()).thenReturn("inquiry2");
    when(moodHistory2.getInquiry()).thenReturn("오늘 기분이 어떠신가요?");

    // null-safe argThat 사용
    when(moodHistoryRepository.save(
            argThat(
                mh -> {
                  if (mh == null) return false;
                  return "user1".equals(mh.getUserId());
                })))
        .thenReturn(moodHistory1);

    when(moodHistoryRepository.save(
            argThat(
                mh -> {
                  if (mh == null) return false;
                  return "user2".equals(mh.getUserId());
                })))
        .thenReturn(moodHistory2);

    when(chatMessageService.sendMessage(any(ChatMessageRequest.class))).thenReturn(null);

    // When
    moodInquiryService.sendMoodInquiryToAllUsers();

    // Then
    verify(chatRoomRepository, times(1))
        .findActiveChatRoomsByType(eq(ChatRoomType.AI), anyInt(), anyInt());
    verify(moodHistoryRepository, times(2)).save(any(UserMoodHistory.class));
    verify(chatMessageService, times(2)).sendMessage(any(ChatMessageRequest.class));

    // 파라미터가 올바르게 전달되는지 확인
    ArgumentCaptor<ChatMessageRequest> messageCaptor =
        ArgumentCaptor.forClass(ChatMessageRequest.class);
    verify(chatMessageService, times(2)).sendMessage(messageCaptor.capture());

    List<ChatMessageRequest> capturedMessages = messageCaptor.getAllValues();
    assertTrue(
        capturedMessages.stream()
            .allMatch(
                msg ->
                    msg != null
                        && msg.getSenderId() != null
                        && msg.getSenderId().equals(AI_SENDER_ID)
                        && msg.getSenderName() != null
                        && msg.getSenderName().equals(AI_SENDER_NAME)
                        && msg.getMessageType() == MessageType.TEXT));
  }

  // 로그인 시 자동 기분 질문 기능 제거됨 - 해당 테스트 삭제

  @Test
  @DisplayName("오늘의 기분 조사 조회 테스트")
  void getTodayMoodInquiry_returnsInquiry() {
    // Given
    List<UserMoodHistory> todayInquiries = Arrays.asList(moodHistory);
    when(moodHistoryRepository.findByUserIdAndCreatedAtBetween(eq(TEST_USER_ID), any(), any()))
        .thenReturn(todayInquiries);

    // When
    Optional<UserMoodHistory> result = moodInquiryService.getTodayMoodInquiry(TEST_USER_ID);

    // Then
    assertTrue(result.isPresent());
    assertEquals(moodHistory, result.get());
  }

  @Test
  @DisplayName("오늘의 미답변 기분 조사 조회 테스트")
  void getTodayUnansweredInquiry_returnsUnansweredInquiry() {
    // Given
    UserMoodHistory unansweredMoodHistory = mock(UserMoodHistory.class);
    when(unansweredMoodHistory.getUserAnswer()).thenReturn(null);

    List<UserMoodHistory> todayInquiries = Arrays.asList(unansweredMoodHistory);
    when(moodHistoryRepository.findByUserIdAndCreatedAtBetween(eq(TEST_USER_ID), any(), any()))
        .thenReturn(todayInquiries);

    // When
    Optional<UserMoodHistory> result = moodInquiryService.getTodayUnansweredInquiry(TEST_USER_ID);

    // Then
    assertTrue(result.isPresent());
    assertEquals(unansweredMoodHistory, result.get());
  }

  @Test
  @DisplayName("미답변 기분 조사 ID 조회 테스트")
  void getPendingInquiryId_returnsPendingId() {
    // Given
    String inquiryId = "test-inquiry-id";
    UserMoodHistory unansweredMoodHistory = mock(UserMoodHistory.class);
    when(unansweredMoodHistory.getUserAnswer()).thenReturn(null);
    when(unansweredMoodHistory.getInquiryId()).thenReturn(inquiryId);

    List<UserMoodHistory> todayInquiries = Arrays.asList(unansweredMoodHistory);
    when(moodHistoryRepository.findByUserIdAndCreatedAtBetween(eq(TEST_USER_ID), any(), any()))
        .thenReturn(todayInquiries);

    // When
    Optional<String> result = moodInquiryService.getPendingInquiryId(TEST_USER_ID);

    // Then
    assertTrue(result.isPresent());
    assertEquals(inquiryId, result.get());
  }

  @Test
  @DisplayName("기분 조사 대기 상태 확인 테스트")
  void hasPendingMoodInquiry_returnsTrueWhenPending() {
    // Given
    UserMoodHistory unansweredMoodHistory = mock(UserMoodHistory.class);
    when(unansweredMoodHistory.getUserAnswer()).thenReturn(null);

    List<UserMoodHistory> todayInquiries = Arrays.asList(unansweredMoodHistory);
    when(moodHistoryRepository.findByUserIdAndCreatedAtBetween(eq(TEST_USER_ID), any(), any()))
        .thenReturn(todayInquiries);

    // When
    boolean result = moodInquiryService.hasPendingMoodInquiry(TEST_USER_ID);

    // Then
    assertTrue(result);
  }
}
