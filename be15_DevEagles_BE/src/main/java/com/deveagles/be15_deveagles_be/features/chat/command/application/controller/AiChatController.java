package com.deveagles.be15_deveagles_be.features.chat.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatRoomResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.AiChatService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.MoodInquiryService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai-chat")
public class AiChatController {

  private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

  private final ChatRoomService chatRoomService;
  private final ChatMessageService chatMessageService;
  private final AiChatService aiChatService;
  private final MoodInquiryService moodInquiryService;

  public AiChatController(
      ChatRoomService chatRoomService,
      ChatMessageService chatMessageService,
      AiChatService aiChatService,
      MoodInquiryService moodInquiryService) {
    this.chatRoomService = chatRoomService;
    this.chatMessageService = chatMessageService;
    this.aiChatService = aiChatService;
    this.moodInquiryService = moodInquiryService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGetAiChatRoom(
      @RequestParam String userId,
      @RequestParam(required = false, defaultValue = "수리AI") String aiName) {

    try {
      ChatRoomResponse chatRoom =
          chatRoomService.createOrGetPersonalAiChatRoom(null, userId, aiName);
      aiChatService.initializeAiChatSession(userId, chatRoom.getId());

      log.info("AI 채팅방 생성/조회 성공 - 사용자: {}, 채팅방: {}", userId, chatRoom.getId());
      return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chatRoom));
    } catch (IllegalArgumentException e) {
      log.warn("AI 채팅방 생성 실패 - 잘못된 파라미터: {}", e.getMessage());
      return ResponseEntity.badRequest()
          .body(ApiResponse.failure("INVALID_PARAMETER", e.getMessage()));
    } catch (Exception e) {
      log.error("AI 채팅방 생성 중 예상치 못한 오류 발생", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.failure("CHATROOM_CREATION_FAILED", "채팅방 생성에 실패했습니다."));
    }
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getUserAiChatRooms(
      @RequestParam String userId, @AuthenticationPrincipal CustomUser customUser) {

    try {
      if (customUser != null && !String.valueOf(customUser.getUserId()).equals(userId)) {
        userId = String.valueOf(customUser.getUserId());
      }

      // TODO: 실제 구현에서는 사용자의 AI 채팅방 목록을 조회하는 서비스 메서드 필요
      log.info("사용자 AI 채팅방 목록 조회 - 사용자: {}", userId);
      return ResponseEntity.ok(ApiResponse.success(List.of()));
    } catch (Exception e) {
      log.error("AI 채팅방 목록 조회 중 오류 발생 - 사용자: {}", userId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.failure("CHATROOM_LIST_FAILED", "채팅방 목록 조회에 실패했습니다."));
    }
  }

  @GetMapping("/{chatroomId}/messages")
  public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getAiChatMessages(
      @PathVariable String chatroomId,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "20") int size,
      @AuthenticationPrincipal CustomUser customUser) {

    try {
      // TODO: 실제 구현에서는 채팅방 접근 권한 검증 필요
      List<ChatMessageResponse> messages =
          chatMessageService.getMessagesByChatroom(chatroomId, page, size);

      log.info("AI 채팅 메시지 조회 성공 - 채팅방: {}, 메시지 수: {}", chatroomId, messages.size());
      return ResponseEntity.ok(ApiResponse.success(messages));
    } catch (IllegalArgumentException e) {
      log.warn("AI 채팅 메시지 조회 실패 - 잘못된 파라미터: {}", e.getMessage());
      return ResponseEntity.badRequest()
          .body(ApiResponse.failure("INVALID_PARAMETER", e.getMessage()));
    } catch (Exception e) {
      log.error("AI 채팅 메시지 조회 중 오류 발생 - 채팅방: {}", chatroomId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.failure("MESSAGE_FETCH_FAILED", "메시지 조회에 실패했습니다."));
    }
  }

  @PostMapping("/mood-inquiry")
  public ResponseEntity<ApiResponse<UserMoodHistory>> generateMoodInquiry(
      @RequestParam String userId, @AuthenticationPrincipal CustomUser customUser) {

    try {
      if (customUser != null && !String.valueOf(customUser.getUserId()).equals(userId)) {
        userId = String.valueOf(customUser.getUserId());
      }

      UserMoodHistory moodInquiry = moodInquiryService.generateMoodInquiry(userId);

      log.info("기분 조사 생성 성공 - 사용자: {}, 조사 ID: {}", userId, moodInquiry.getId());
      return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(moodInquiry));
    } catch (IllegalStateException e) {
      log.warn("기분 조사 생성 실패 - 이미 존재: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.failure("MOOD_INQUIRY_EXISTS", e.getMessage()));
    } catch (IllegalArgumentException e) {
      log.warn("기분 조사 생성 실패 - 잘못된 파라미터: {}", e.getMessage());
      return ResponseEntity.badRequest()
          .body(ApiResponse.failure("INVALID_PARAMETER", e.getMessage()));
    } catch (Exception e) {
      log.error("기분 조사 생성 중 예상치 못한 오류 발생 - 사용자: {}", userId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.failure("MOOD_INQUIRY_FAILED", "기분 조사 생성에 실패했습니다."));
    }
  }

  @PostMapping("/mood-answer")
  public ResponseEntity<ApiResponse<UserMoodHistory>> saveMoodAnswer(
      @RequestBody Map<String, String> requestBody,
      @AuthenticationPrincipal CustomUser customUser) {

    try {
      String inquiryId = requestBody.get("inquiryId");
      String answer = requestBody.get("answer");

      if (inquiryId == null
          || answer == null
          || inquiryId.trim().isEmpty()
          || answer.trim().isEmpty()) {
        log.warn("기분 답변 저장 실패 - 필수 파라미터 누락: inquiryId={}, answer={}", inquiryId, answer);
        return ResponseEntity.badRequest()
            .body(ApiResponse.failure("INVALID_REQUEST", "inquiryId와 answer는 필수 입력값입니다."));
      }

      UserMoodHistory savedMood = moodInquiryService.saveMoodAnswer(inquiryId, answer);

      log.info("기분 답변 저장 성공 - 조사 ID: {}, 기분: {}", inquiryId, savedMood.getMoodType());
      return ResponseEntity.ok(ApiResponse.success(savedMood));
    } catch (IllegalArgumentException e) {
      log.warn("기분 답변 저장 실패 - 조사를 찾을 수 없음: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.failure("INQUIRY_NOT_FOUND", e.getMessage()));
    } catch (Exception e) {
      log.error("기분 답변 저장 중 예상치 못한 오류 발생", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.failure("MOOD_ANSWER_FAILED", "기분 답변 저장에 실패했습니다."));
    }
  }

  @GetMapping("/mood-history")
  public ResponseEntity<ApiResponse<List<UserMoodHistory>>> getUserMoodHistory(
      @RequestParam String userId, @AuthenticationPrincipal CustomUser customUser) {

    try {
      if (customUser != null && !String.valueOf(customUser.getUserId()).equals(userId)) {
        userId = String.valueOf(customUser.getUserId());
      }

      List<UserMoodHistory> moodHistory = moodInquiryService.getUserMoodHistory(userId);

      log.info("기분 이력 조회 성공 - 사용자: {}, 이력 수: {}", userId, moodHistory.size());
      return ResponseEntity.ok(ApiResponse.success(moodHistory));
    } catch (IllegalArgumentException e) {
      log.warn("기분 이력 조회 실패 - 잘못된 파라미터: {}", e.getMessage());
      return ResponseEntity.badRequest()
          .body(ApiResponse.failure("INVALID_PARAMETER", e.getMessage()));
    } catch (Exception e) {
      log.error("기분 이력 조회 중 예상치 못한 오류 발생 - 사용자: {}", userId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.failure("MOOD_HISTORY_FAILED", "기분 이력 조회에 실패했습니다."));
    }
  }

  // 전역 예외 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception e) {
    log.error("AI 채팅 컨트롤러에서 처리되지 않은 예외 발생", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.failure("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
  }
}
