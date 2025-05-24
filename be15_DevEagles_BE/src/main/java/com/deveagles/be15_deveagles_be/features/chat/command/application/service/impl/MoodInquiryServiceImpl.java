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
    log.info("사용자 {} 기분 질문 생성", userId);

    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
    LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

    boolean alreadyAskedToday =
        moodHistoryRepository.existsByUserIdAndCreatedAtBetween(userId, todayStart, todayEnd);

    if (alreadyAskedToday) {
      log.info("사용자 {}에게 오늘 이미 기분 질문이 전송됨", userId);
      throw new IllegalStateException("이미 오늘 기분 질문이 전송되었습니다.");
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
    return moodHistoryRepository
        .findByInquiryId(inquiryId)
        .map(
            moodHistory -> {
              moodHistory.answer(userAnswer);

              EmotionAnalysisService.EmotionAnalysisResult analysisResult =
                  emotionAnalysisService.analyzeEmotion(userAnswer);

              if (!analysisResult.isEmpty()) {
                moodHistory.setEmotionAnalysis(analysisResult.getRawJson());
                moodHistory.updateMoodTypeAndIntensity(
                    analysisResult.getMoodType(), analysisResult.getIntensity());
              }

              return moodHistoryRepository.save(moodHistory);
            })
        .orElseThrow(() -> new IllegalArgumentException("해당 질문 ID가 존재하지 않습니다: " + inquiryId));
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
      } catch (IllegalStateException e) {
        // 이미 오늘 기분 질문이 전송된 경우 - 정상적인 상황
        log.debug("사용자 {}는 이미 오늘 기분 질문을 받았음: {}", userId, e.getMessage());
      } catch (Exception e) {
        log.error("사용자 {}에게 기분 질문 전송 실패: {}", userId, e.getMessage(), e);
      }
    }
  }

  public void sendMoodInquiryOnUserLogin(String userId) {
    log.info("사용자 {} 로그인 감지, 기분 질문 필요성 확인", userId);

    try {
      UserMoodHistory moodInquiry = generateMoodInquiry(userId);
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
    } catch (IllegalStateException e) {
      // 이미 오늘 기분 질문이 전송된 경우 - 정상적인 상황
      log.debug("로그인한 사용자 {}는 이미 오늘 기분 질문을 받았음: {}", userId, e.getMessage());
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
}
