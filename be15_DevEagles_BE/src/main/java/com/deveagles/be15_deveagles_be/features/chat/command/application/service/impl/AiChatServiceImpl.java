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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
  private static final int MAX_CACHE_SIZE = 1000; // 최대 캐시 크기

  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final ChatMessageService chatMessageService;
  private final UserMoodHistoryRepository moodHistoryRepository;
  private final MoodInquiryService moodInquiryService;
  private final GeminiApiAdapter geminiApiAdapter;

  private final Map<String, Map<String, Object>> userChatContexts =
      new LinkedHashMap<String, Map<String, Object>>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Map<String, Object>> eldest) {
          if (size() > MAX_CACHE_SIZE) {
            log.info("LRU 캐시 한계 도달 - 가장 오래된 세션 제거: {}", eldest.getKey());
            return true;
          }
          return false;
        }
      };

  private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);

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

  @PostConstruct
  public void init() {
    cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredSessions, 1, 1, TimeUnit.HOURS);
    log.info("AI 채팅 서비스 초기화 - 백그라운드 정리 작업 시작");
  }

  @PreDestroy
  public void destroy() {
    cleanupExecutor.shutdown();
    try {
      if (!cleanupExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
        cleanupExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      cleanupExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }
    log.info("AI 채팅 서비스 종료 - 백그라운드 정리 작업 중단");
  }

  private String getContextKey(String userId, String chatroomId) {
    return userId + ":" + chatroomId;
  }

  private synchronized void updateSessionActivity(String userId, String chatroomId) {
    String contextKey = getContextKey(userId, chatroomId);
    Map<String, Object> context = userChatContexts.get(contextKey);
    if (context != null) {
      context.put("lastActivity", LocalDateTime.now());
      Integer messageCount = (Integer) context.getOrDefault("messageCount", 0);
      context.put("messageCount", messageCount + 1);
    }
  }

  private synchronized void cleanupExpiredSessions() {
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

    keysToRemove.forEach(
        key -> {
          userChatContexts.remove(key);
          log.info("만료된 AI 채팅 세션 정리: {}", key);
        });

    if (!keysToRemove.isEmpty()) {
      log.info(
          "총 {}개의 만료된 AI 채팅 세션 정리됨. 현재 활성 세션: {}", keysToRemove.size(), userChatContexts.size());
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

    try {
      GeminiTextResponse response = geminiApiAdapter.generateText(prompt);
      if (response != null && !response.isEmpty()) {
        return response.getText();
      }
    } catch (Exception e) {
      log.warn("Gemini API 호출 실패, 기본 응답 사용: {}", e.getMessage());
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

    // 1. 기분 조사 답변 처리 (MoodInquiryService에 위임)
    Optional<String> pendingInquiryId =
        moodInquiryService.getPendingInquiryId(userMessage.getSenderId());
    if (pendingInquiryId.isPresent()) {
      try {
        UserMoodHistory savedMood =
            moodInquiryService.saveMoodAnswer(pendingInquiryId.get(), userMessage.getContent());

        log.info(
            "기분 조사 답변 처리 완료 - 사용자: {}, 기분: {}", userMessage.getSenderId(), savedMood.getMoodType());

        return generateMoodFeedbackResponse(userMessage, savedMood);
      } catch (Exception e) {
        log.error("기분 조사 답변 처리 실패", e);
        // 실패해도 일반 AI 응답으로 계속 진행
      }
    }

    // 2. 일반 AI 응답 생성
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
}
