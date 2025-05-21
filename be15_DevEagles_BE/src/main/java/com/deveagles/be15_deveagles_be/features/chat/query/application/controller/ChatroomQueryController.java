package com.deveagles.be15_deveagles_be.features.chat.query.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomListResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomReadSummaryResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.service.ChatroomQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatrooms")
public class ChatroomQueryController {

  private final ChatroomQueryService chatroomQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<ChatroomListResponse>> getChatrooms(
      @AuthenticationPrincipal CustomUser customUser,
      @RequestParam(required = false) String teamId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    log.info("채팅방 목록 조회 요청 -> 사용자: {}, 팀ID: {}", customUser.getUsername(), teamId);
    ChatroomListResponse response =
        chatroomQueryService.getChatrooms(customUser.getUserId(), teamId, page, size);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/{chatroomId}")
  public ResponseEntity<ApiResponse<ChatroomResponse>> getChatroom(
      @AuthenticationPrincipal CustomUser customUser, @PathVariable String chatroomId) {
    log.info("채팅방 상세 조회 요청 -> 사용자: {}, 채팅방ID: {}", customUser.getUsername(), chatroomId);
    ChatroomResponse response =
        chatroomQueryService.getChatroom(customUser.getUserId(), chatroomId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/{chatroomId}/read-summary")
  public ResponseEntity<ApiResponse<ChatroomReadSummaryResponse>> getChatroomReadSummary(
      @AuthenticationPrincipal CustomUser customUser, @PathVariable String chatroomId) {
    log.info("채팅방 읽음 상태 요약 조회 요청 -> 사용자: {}, 채팅방ID: {}", customUser.getUsername(), chatroomId);
    ChatroomReadSummaryResponse response =
        chatroomQueryService.getChatroomReadSummary(customUser.getUserId(), chatroomId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
