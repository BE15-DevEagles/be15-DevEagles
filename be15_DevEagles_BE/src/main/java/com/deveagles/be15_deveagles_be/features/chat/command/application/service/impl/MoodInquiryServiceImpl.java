package com.deveagles.be15_deveagles_be.features.chat.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.ChatMessageRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
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
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.HuggingFaceApiAdapter;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.adapters.HuggingFaceApiAdapter.EmotionAnalysisResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MoodInquiryServiceImpl implements MoodInquiryService {

  private static final Logger log = LoggerFactory.getLogger(MoodInquiryServiceImpl.class);
  private static final String AI_SENDER_ID = "ai-assistant";
  private static final String AI_SENDER_NAME = "수리 AI";

  private static final ThreadLocal<String> currentInquiryIdContext = new ThreadLocal<>();

  private final UserMoodHistoryRepository moodHistoryRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomService chatRoomService;
  private final ChatMessageService chatMessageService;
  private final GeminiApiAdapter geminiApiAdapter;
  private final HuggingFaceApiAdapter huggingFaceApiAdapter;

  public MoodInquiryServiceImpl(
      UserMoodHistoryRepository moodHistoryRepository,
      ChatRoomRepository chatRoomRepository,
      ChatRoomService chatRoomService,
      ChatMessageService chatMessageService,
      GeminiApiAdapter geminiApiAdapter,
      HuggingFaceApiAdapter huggingFaceApiAdapter) {
    this.moodHistoryRepository = moodHistoryRepository;
    this.chatRoomRepository = chatRoomRepository;
    this.chatRoomService = chatRoomService;
    this.chatMessageService = chatMessageService;
    this.geminiApiAdapter = geminiApiAdapter;
    this.huggingFaceApiAdapter = huggingFaceApiAdapter;
  }

  @Override
  public UserMoodHistory generateMoodInquiry(String userId) {
    log.info("사용자 {} 기분 질문 생성", userId);

    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
    LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

    boolean alreadyAskedToday =
        moodHistoryRepository.existsByUserIdAndCreatedAtBetween(userId, todayStart, todayEnd);

    if (alreadyAskedToday) {
      log.info("사용자 {}에게 오늘 이미 기분 질문이 전송됨", userId);
      return null;
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

    return moodHistoryRepository.save(moodHistory);
  }

  @Override
  public UserMoodHistory saveMoodAnswer(String inquiryId, String userAnswer) {
    try {
      setCurrentInquiryId(inquiryId);

      return moodHistoryRepository
          .findByInquiryId(inquiryId)
          .map(
              moodHistory -> {
                moodHistory.answer(userAnswer);

                MoodType detectedMood = analyzeEmotionWithHuggingFace(userAnswer);

                if (moodHistory.getEmotionAnalysis().isEmpty()) {
                  updateMoodType(moodHistory, detectedMood);
                }

                return moodHistoryRepository.save(moodHistory);
              })
          .orElse(null);
    } finally {
      clearCurrentInquiryId();
    }
  }

  private void setCurrentInquiryId(String inquiryId) {
    currentInquiryIdContext.set(inquiryId);
  }

  private void clearCurrentInquiryId() {
    currentInquiryIdContext.remove();
  }

  private String findInquiryIdFromContext() {
    return currentInquiryIdContext.get();
  }

  @Override
  public List<UserMoodHistory> getUserMoodHistory(String userId) {
    return moodHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  @Override
  public void sendMoodInquiryToAllUsers() {
    log.info("현재 로그인한 모든 사용자에게 기분 질문 전송 작업 시작");

    List<String> usersWithAiChatRooms =
        chatRoomRepository.findActiveChatRoomsByType(ChatRoomType.AI, 0, 100).getContent().stream()
            .map(room -> room.getUserId())
            .distinct()
            .collect(Collectors.toList());

    for (String userId : usersWithAiChatRooms) {
      try {
        UserMoodHistory moodInquiry = generateMoodInquiry(userId);
        if (moodInquiry != null) {
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
                            .metadata(Map.of("inquiryId", moodInquiry.getInquiryId()))
                            .build();

                    chatMessageService.sendMessage(messageRequest);
                    log.info("사용자 {}에게 기분 질문 전송 완료: {}", userId, moodInquiry.getInquiry());
                  });
        }
      } catch (Exception e) {
        log.error("사용자 {}에게 기분 질문 전송 실패: {}", userId, e.getMessage(), e);
      }
    }
  }

  public void sendMoodInquiryOnUserLogin(String userId) {
    log.info("사용자 {} 로그인 감지, 기분 질문 필요성 확인", userId);

    try {
      UserMoodHistory moodInquiry = generateMoodInquiry(userId);
      if (moodInquiry != null) {
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
                          .metadata(Map.of("inquiryId", moodInquiry.getInquiryId()))
                          .build();

                  chatMessageService.sendMessage(messageRequest);
                  log.info("로그인한 사용자 {}에게 기분 질문 전송: {}", userId, moodInquiry.getInquiry());
                });
      }
    } catch (Exception e) {
      log.error("로그인한 사용자 {}에게 기분 질문 전송 실패: {}", userId, e.getMessage(), e);
    }
  }

  private String generateMoodQuestion() {
    GeminiTextResponse response =
        geminiApiAdapter.generateText(PromptTemplate.getMoodQuestionPrompt());
    if (!response.isEmpty()) {
      return response.getText();
    }
    return PromptTemplate.getRandomMoodInquiry();
  }

  private MoodType analyzeEmotionWithHuggingFace(String answer) {
    String inquiryId = findInquiryIdFromContext();

    EmotionAnalysisResponse response = huggingFaceApiAdapter.analyzeEmotion(answer);

    if (inquiryId != null && !response.isEmpty()) {
      moodHistoryRepository
          .findByInquiryId(inquiryId)
          .ifPresent(
              moodHistory -> {
                moodHistory.setEmotionAnalysis(response.getRawJson());
                moodHistoryRepository.save(moodHistory);
                log.info("UserMoodHistory(ID: {})에 감정 분석 결과 저장 완료", moodHistory.getId());
              });
    }

    return convertToMoodType(response);
  }

  private MoodType convertToMoodType(EmotionAnalysisResponse response) {
    if (response.isEmpty()) {
      return MoodType.NEUTRAL;
    }

    try {
      String dominantEmotion = response.getDominantEmotion().toUpperCase();
      return MoodType.valueOf(dominantEmotion);
    } catch (IllegalArgumentException e) {
      log.warn("알 수 없는 감정 라벨: {}, NEUTRAL로 설정됨", response.getDominantEmotion());
      return MoodType.NEUTRAL;
    }
  }

  private void updateMoodType(UserMoodHistory moodHistory, MoodType newMoodType) {
    try {
      java.lang.reflect.Field field = UserMoodHistory.class.getDeclaredField("moodType");
      field.setAccessible(true);
      field.set(moodHistory, newMoodType);
    } catch (Exception e) {
      log.error("moodType 업데이트 실패", e);
    }
  }
}
