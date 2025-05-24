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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    ChatRoomResponse chatRoom = chatRoomService.createOrGetPersonalAiChatRoom(null, userId, aiName);

    aiChatService.initializeAiChatSession(userId, chatRoom.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chatRoom));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getUserAiChatRooms(
      @RequestParam String userId, @AuthenticationPrincipal CustomUser customUser) {

    if (customUser != null && !String.valueOf(customUser.getUserId()).equals(userId)) {
      userId = String.valueOf(customUser.getUserId());
    }

    // TODO: 실제 구현에서는 사용자의 AI 채팅방 목록을 조회하는 서비스 메서드 필요

    return ResponseEntity.ok(ApiResponse.success(List.of()));
  }

  @GetMapping("/{chatroomId}/messages")
  public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getAiChatMessages(
      @PathVariable String chatroomId,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "20") int size,
      @AuthenticationPrincipal CustomUser customUser) {

    // TODO: 실제 구현에서는 채팅방 접근 권한 검증 필요

    List<ChatMessageResponse> messages =
        chatMessageService.getMessagesByChatroom(chatroomId, page, size);
    return ResponseEntity.ok(ApiResponse.success(messages));
  }

  @PostMapping("/mood-inquiry")
  public ResponseEntity<ApiResponse<UserMoodHistory>> generateMoodInquiry(
      @RequestParam String userId, @AuthenticationPrincipal CustomUser customUser) {

    if (customUser != null && !String.valueOf(customUser.getUserId()).equals(userId)) {
      userId = String.valueOf(customUser.getUserId());
    }

    try {
      UserMoodHistory moodInquiry = moodInquiryService.generateMoodInquiry(userId);
      return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(moodInquiry));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.failure("MOOD_INQUIRY_EXISTS", e.getMessage()));
    }
  }

  @PostMapping("/mood-answer")
  public ResponseEntity<ApiResponse<UserMoodHistory>> saveMoodAnswer(
      @RequestBody Map<String, String> requestBody,
      @AuthenticationPrincipal CustomUser customUser) {

    String inquiryId = requestBody.get("inquiryId");
    String answer = requestBody.get("answer");

    if (inquiryId == null || answer == null) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.failure("INVALID_REQUEST", "inquiryId와 answer는 필수 입력값입니다."));
    }

    try {
      UserMoodHistory savedMood = moodInquiryService.saveMoodAnswer(inquiryId, answer);
      return ResponseEntity.ok(ApiResponse.success(savedMood));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.failure("INQUIRY_NOT_FOUND", e.getMessage()));
    }
  }

  @GetMapping("/mood-history")
  public ResponseEntity<ApiResponse<List<UserMoodHistory>>> getUserMoodHistory(
      @RequestParam String userId, @AuthenticationPrincipal CustomUser customUser) {

    if (customUser != null && !String.valueOf(customUser.getUserId()).equals(userId)) {
      userId = String.valueOf(customUser.getUserId());
    }

    List<UserMoodHistory> moodHistory = moodInquiryService.getUserMoodHistory(userId);
    return ResponseEntity.ok(ApiResponse.success(moodHistory));
  }
}
