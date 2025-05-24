package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.EmotionAnalysisService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.MoodInquiryService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.PromptTemplate;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage.MessageType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory.MoodType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.UserMoodHistoryRepository;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.GeminiApiAdapter;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.GeminiApiAdapter.GeminiTextResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MoodInquiryServiceImpl implements MoodInquiryService {

  private static final Logger log = LoggerFactory.getLogger(MoodInquiryServiceImpl.class);
  private static final String AI_SENDER_ID = "ai-assistant";
  private static final String AI_SENDER_NAME = "수리 AI";

  private final UserMoodHistoryRepository moodHistoryRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomService chatRoomService;
  private final ChatMessageService chatMessageService;
  private final GeminiApiAdapter geminiApiAdapter;
  private final EmotionAnalysisService emotionAnalysisService;

  public MoodInquiryServiceImpl(
      UserMoodHistoryRepository moodHistoryRepository,
      ChatRoomRepository chatRoomRepository,
      ChatRoomService chatRoomService,
      ChatMessageService chatMessageService,
      GeminiApiAdapter geminiApiAdapter,
      EmotionAnalysisService emotionAnalysisService) {
    this.moodHistoryRepository = moodHistoryRepository;
    this.chatRoomRepository = chatRoomRepository;
    this.chatRoomService = chatRoomService;
    this.chatMessageService = chatMessageService;
    this.geminiApiAdapter = geminiApiAdapter;
    this.emotionAnalysisService = emotionAnalysisService;
  }

  @Override
  public UserMoodHistory generateMoodInquiry(String userId) {
    log.info("사용자 {} 기분 질문 생성 시작", userId);

    // 오늘 이미 질문이 있는지확인
    Optional<UserMoodHistory> todayInquiry = getTodayMoodInquiry(userId);
    if (todayInquiry.isPresent()) {
      log.info("사용자 {}에게 오늘 이미 기분 질문이 생성됨: {}", userId, todayInquiry.get().getInquiryId());
      throw new IllegalStateException("이미 오늘 기분 질문이 생성되었습니다.");
    }

    String inquiry = generateMoodQuestion();
    String inquiryId = UUID.randomUUID().toString();

    UserMoodHistory moodHistory =
        UserMoodHistory.builder()
            .userId(userId)
            .moodType(MoodType.NEUTRAL)
            .inquiry(inquiry)
            .inquiryId(inquiryId)
            .createdAt(LocalDateTime.now())
            .build();

    UserMoodHistory saved = moodHistoryRepository.save(moodHistory);
    log.info("기분 질문 생성 완료 - 사용자: {}, 질문ID: {}", userId, inquiryId);

    return saved;
  }

  @Override
  public UserMoodHistory saveMoodAnswer(String inquiryId, String userAnswer) {
    log.info("기분 답변 저장 시작 - 질문ID: {}", inquiryId);

    UserMoodHistory moodHistory =
        moodHistoryRepository
            .findByInquiryId(inquiryId)
            .orElseThrow(() -> new IllegalArgumentException("해당 질문 ID가 존재하지 않습니다: " + inquiryId));

    // 이미 답변했는지 확인
    if (moodHistory.getUserAnswer() != null) {
      log.warn("이미 답변된 기분 질문 - 질문ID: {}", inquiryId);
      throw new IllegalStateException("이미 답변된 질문입니다.");
    }

    // 답변 저장
    moodHistory.answer(userAnswer);

    // 감정 분석 수행
    try {
      EmotionAnalysisService.EmotionAnalysisResult analysisResult =
          emotionAnalysisService.analyzeEmotion(userAnswer);

      if (!analysisResult.isEmpty()) {
        moodHistory.setEmotionAnalysis(analysisResult.getRawJson());
        moodHistory.updateMoodTypeAndIntensity(
            analysisResult.getMoodType(), analysisResult.getIntensity());

        log.info(
            "감정 분석 완료 - 질문ID: {}, 기분: {}, 강도: {}",
            inquiryId,
            analysisResult.getMoodType(),
            analysisResult.getIntensity());
      }
    } catch (Exception e) {
      log.error("감정 분석 실패 - 질문ID: {}", inquiryId, e);
      // 감정 분석 실패해도 답변은 저장됨
    }

    UserMoodHistory saved = moodHistoryRepository.save(moodHistory);
    log.info("기분 답변 저장 완료 - 질문ID: {}, 사용자: {}", inquiryId, saved.getUserId());

    return saved;
  }

  @Override
  public List<UserMoodHistory> getUserMoodHistory(String userId) {
    return moodHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  @Override
  public void sendMoodInquiryToAllUsers() {
    log.info("모든 사용자에게 기분 질문 전송 작업 시작");

    List<String> usersWithAiChatRooms = getActiveAiChatUsers();
    int successCount = 0;
    int skipCount = 0;

    for (String userId : usersWithAiChatRooms) {
      try {
        UserMoodHistory moodInquiry = generateMoodInquiry(userId);
        sendMoodInquiryMessage(userId, moodInquiry);
        successCount++;
      } catch (IllegalStateException e) {
        log.debug("사용자 {}는 이미 오늘 기분 질문을 받았음", userId);
        skipCount++;
      } catch (Exception e) {
        log.error("사용자 {}에게 기분 질문 전송 실패", userId, e);
      }
    }

    log.info(
        "기분 질문 전송 완료 - 성공: {}명, 건너뜀: {}명, 전체: {}명",
        successCount,
        skipCount,
        usersWithAiChatRooms.size());
  }

  public Optional<UserMoodHistory> getTodayMoodInquiry(String userId) {
    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
    LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

    List<UserMoodHistory> todayInquiries =
        moodHistoryRepository.findByUserIdAndCreatedAtBetween(userId, todayStart, todayEnd);

    return todayInquiries.stream().findFirst();
  }

  public Optional<UserMoodHistory> getTodayUnansweredInquiry(String userId) {
    return getTodayMoodInquiry(userId).filter(inquiry -> inquiry.getUserAnswer() == null);
  }

  public boolean hasPendingMoodInquiry(String userId) {
    return getTodayUnansweredInquiry(userId).isPresent();
  }

  public Optional<String> getPendingInquiryId(String userId) {
    return getTodayUnansweredInquiry(userId).map(UserMoodHistory::getInquiryId);
  }

  private String generateMoodQuestion() {
    try {
      GeminiTextResponse response =
          geminiApiAdapter.generateText(PromptTemplate.MOOD_INQUIRY_PROMPT);
      if (!response.isEmpty()) {
        return response.getText();
      }
    } catch (Exception e) {
      log.warn("AI 기분 질문 생성 실패, 기본 질문 사용", e);
    }

    return PromptTemplate.getDefaultMoodQuestion();
  }

  private List<String> getActiveAiChatUsers() {
    return chatRoomRepository
        .findActiveChatRoomsByType(ChatRoomType.AI, 0, 100)
        .getContent()
        .stream()
        .map(room -> room.getUserId())
        .distinct()
        .collect(Collectors.toList());
  }

  private void sendMoodInquiryMessage(String userId, UserMoodHistory moodInquiry) {
    chatRoomRepository
        .findByTeamIdAndUserIdAndTypeAndDeletedAtIsNull(null, userId, ChatRoomType.AI)
        .ifPresent(
            chatRoom -> {
              ChatMessageRequest messageRequest =
                  ChatMessageRequest.builder()
                      .chatroomId(chatRoom.getId())
                      .senderId(AI_SENDER_ID)
                      .senderName(AI_SENDER_NAME)
                      .messageType(MessageType.TEXT)
                      .content(moodInquiry.getInquiry())
                      .metadata(
                          Map.of("inquiryId", moodInquiry.getInquiryId(), "type", "mood_inquiry"))
                      .build();

              chatMessageService.sendMessage(messageRequest);
              log.info("사용자 {}에게 기분 질문 전송 완료: {}", userId, moodInquiry.getInquiry());
            });
  }
}
