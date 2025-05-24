package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.AiChatService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.MoodInquiryService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.PromptTemplate;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage.MessageType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatMessageRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.UserMoodHistoryRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.GeminiApiAdapter;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.GeminiApiAdapter.GeminiTextResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class AiChatServiceImpl implements AiChatService {

  private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);
  private static final String AI_NAME = "수리 AI";
  private static final String AI_USER_ID = "ai-assistant";
  private static final long SESSION_TIMEOUT_HOURS = 24;

  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final ChatMessageService chatMessageService;
  private final UserMoodHistoryRepository moodHistoryRepository;
  private final MoodInquiryService moodInquiryService;
  private final GeminiApiAdapter geminiApiAdapter;

  private final Map<String, Map<String, Object>> userChatContexts = new ConcurrentHashMap<>();

  public AiChatServiceImpl(
      ChatMessageRepository chatMessageRepository,
      ChatRoomRepository chatRoomRepository,
      SimpMessagingTemplate messagingTemplate,
      ChatMessageService chatMessageService,
      UserMoodHistoryRepository moodHistoryRepository,
      MoodInquiryService moodInquiryService,
      GeminiApiAdapter geminiApiAdapter) {
    this.chatMessageRepository = chatMessageRepository;
    this.chatRoomRepository = chatRoomRepository;
    this.messagingTemplate = messagingTemplate;
    this.chatMessageService = chatMessageService;
    this.moodHistoryRepository = moodHistoryRepository;
    this.moodInquiryService = moodInquiryService;
    this.geminiApiAdapter = geminiApiAdapter;
  }

  private String getContextKey(String userId, String chatroomId) {
    return userId + ":" + chatroomId;
  }

  private void updateSessionActivity(String userId, String chatroomId) {
    String contextKey = getContextKey(userId, chatroomId);
    Map<String, Object> context = userChatContexts.get(contextKey);
    if (context != null) {
      context.put("lastActivity", LocalDateTime.now());
      Integer messageCount = (Integer) context.getOrDefault("messageCount", 0);
      context.put("messageCount", messageCount + 1);
    }
  }

  private void cleanupExpiredSessions() {
    LocalDateTime now = LocalDateTime.now();
    List<String> keysToRemove = new ArrayList<>();

    userChatContexts.forEach(
        (key, context) -> {
          LocalDateTime lastActivity =
              (LocalDateTime) context.getOrDefault("lastActivity", context.get("initialized"));
          if (lastActivity != null && lastActivity.plusHours(SESSION_TIMEOUT_HOURS).isBefore(now)) {
            keysToRemove.add(key);
          }
        });

    for (String key : keysToRemove) {
      userChatContexts.remove(key);
      log.info("만료된 AI 채팅 세션 정리: {}", key);
    }
  }

  private Optional<ChatMessage> findLastAiMessageInChatroom(String chatroomId) {
    List<ChatMessage> recentMessages =
        chatMessageRepository.findRecentMessagesByChatroomId(chatroomId, 5);

    return recentMessages.stream().filter(msg -> AI_USER_ID.equals(msg.getSenderId())).findFirst();
  }

  private String buildChatHistory(String chatroomId) {
    List<ChatMessage> recentMessages =
        chatMessageRepository.findRecentMessagesByChatroomId(chatroomId, 10);

    if (recentMessages.isEmpty()) {
      return "";
    }

    StringBuilder historyBuilder = new StringBuilder();
    for (int i = recentMessages.size() - 1; i >= 0; i--) {
      ChatMessage message = recentMessages.get(i);
      String senderName = AI_USER_ID.equals(message.getSenderId()) ? "수리" : "사용자";
      historyBuilder.append(senderName).append(": ").append(message.getContent()).append("\n");
    }

    return historyBuilder.toString().trim();
  }

  private String generateAiResponse(String userMessage, String chatroomId) {
    String chatHistory = buildChatHistory(chatroomId);

    String prompt =
        chatHistory.isEmpty()
            ? PromptTemplate.getAiResponsePrompt(userMessage)
            : PromptTemplate.getAiResponsePromptWithHistory(chatHistory, userMessage);

    GeminiTextResponse response = geminiApiAdapter.generateText(prompt);
    if (!response.isEmpty()) {
      return response.getText();
    }
    return PromptTemplate.getRandomDefaultResponse();
  }

  private String generateMoodFeedbackContent(UserMoodHistory moodHistory) {
    String moodType =
        moodHistory.getMoodType() != null ? moodHistory.getMoodType().toString() : "NEUTRAL";
    return PromptTemplate.getMoodFeedback(moodType);
  }

  private ChatMessageResponse generateMoodFeedbackResponse(
      ChatMessageRequest userMessage, UserMoodHistory moodHistory) {
    String feedbackContent = generateMoodFeedbackContent(moodHistory);

    ChatMessageRequest feedbackRequest =
        ChatMessageRequest.builder()
            .chatroomId(userMessage.getChatroomId())
            .senderId(AI_USER_ID)
            .senderName(AI_NAME)
            .messageType(MessageType.TEXT)
            .content(feedbackContent)
            .build();

    return chatMessageService.sendMessage(feedbackRequest);
  }

  @Override
  public void initializeAiChatSession(String userId, String chatroomId) {
    cleanupExpiredSessions();

    String contextKey = getContextKey(userId, chatroomId);
    Map<String, Object> context = new HashMap<>();
    context.put("initialized", LocalDateTime.now());
    context.put("messageCount", 0);
    context.put("lastActivity", LocalDateTime.now());
    userChatContexts.put(contextKey, context);

    log.info("AI 채팅 세션 초기화 - 사용자: {}, 채팅방: {}", userId, chatroomId);
  }

  @Override
  public void terminateAiChatSession(String userId, String chatroomId) {
    String contextKey = getContextKey(userId, chatroomId);
    userChatContexts.remove(contextKey);
    log.info("AI 채팅 세션 종료 - 사용자: {}, 채팅방: {}", userId, chatroomId);
  }

  @Override
  public ChatMessageResponse processUserMessage(ChatMessageRequest userMessage) {
    log.info("AI 채팅 처리 - 사용자: {}, 메시지: {}", userMessage.getSenderId(), userMessage.getContent());

    updateSessionActivity(userMessage.getSenderId(), userMessage.getChatroomId());

    // 기분 질문에 대한 답변인지 확인
    String inquiryId = findPendingMoodInquiryId(userMessage);
    if (inquiryId != null) {
      UserMoodHistory savedMood =
          moodInquiryService.saveMoodAnswer(inquiryId, userMessage.getContent());
      if (savedMood != null) {
        return generateMoodFeedbackResponse(userMessage, savedMood);
      }
    }

    // 일반 AI 응답 생성
    String aiResponse = generateAiResponse(userMessage.getContent(), userMessage.getChatroomId());

    ChatMessageRequest aiMessageRequest =
        ChatMessageRequest.builder()
            .chatroomId(userMessage.getChatroomId())
            .senderId(AI_USER_ID)
            .senderName(AI_NAME)
            .messageType(MessageType.TEXT)
            .content(aiResponse)
            .build();

    return chatMessageService.sendMessage(aiMessageRequest);
  }

  /** 사용자 메시지가 기분 질문에 대한 답변인지 확인하고 inquiryId 반환 */
  private String findPendingMoodInquiryId(ChatMessageRequest userMessage) {
    // 1. 오늘의 미답변 기분 질문 확인
    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
    LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

    List<UserMoodHistory> todayMoodInquiries =
        moodHistoryRepository.findByUserIdAndCreatedAtBetween(
            userMessage.getSenderId(), todayStart, todayEnd);

    Optional<UserMoodHistory> unansweredInquiry =
        todayMoodInquiries.stream().filter(inquiry -> inquiry.getUserAnswer() == null).findFirst();

    if (unansweredInquiry.isPresent()) {
      return unansweredInquiry.get().getInquiryId();
    }

    // 2. 마지막 AI 메시지에 inquiryId가 있는지 확인
    Optional<ChatMessage> lastAiMessage = findLastAiMessageInChatroom(userMessage.getChatroomId());
    if (lastAiMessage.isPresent()
        && lastAiMessage.get().getMetadata() != null
        && lastAiMessage.get().getMetadata().containsKey("inquiryId")) {
      return (String) lastAiMessage.get().getMetadata().get("inquiryId");
    }

    return null;
  }
}
