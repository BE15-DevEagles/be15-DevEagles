package com.deveagles.be15_deveagles_be.features.chat.query.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageListResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageReadStatusResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.service.MessageQueryService;
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
@RequestMapping("/api/v1/chatrooms/{chatroomId}/messages")
public class MessageQueryController {

  private final MessageQueryService messageQueryService;

  /**
   * 채팅방 메시지 조회 API
   *
   * @param customUser 인증된 사용자 정보
   * @param chatroomId 채팅방 ID
   * @param before 해당 메시지 ID 이전의 메시지를 조회 (null인 경우 최신 메시지부터 조회)
   * @param limit 조회할 메시지 개수 (기본값: 50)
   * @return 메시지 목록 응답
   */
  @GetMapping
  public ResponseEntity<ApiResponse<MessageListResponse>> getMessages(
      @AuthenticationPrincipal CustomUser customUser,
      @PathVariable String chatroomId,
      @RequestParam(required = false) String before,
      @RequestParam(defaultValue = "50") int limit) {
    log.info(
        "메시지 조회 요청 -> 사용자: {}, 채팅방ID: {}, beforeId: {}",
        customUser.getUsername(),
        chatroomId,
        before);
    MessageListResponse response =
        messageQueryService.getMessages(customUser.getUserId(), chatroomId, before, limit);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 메시지 읽음 상태 조회 API
   *
   * @param customUser 인증된 사용자 정보
   * @param chatroomId 채팅방 ID
   * @param messageId 메시지 ID
   * @return 메시지 읽음 상태 응답
   */
  @GetMapping("/{messageId}/read-status")
  public ResponseEntity<ApiResponse<MessageReadStatusResponse>> getMessageReadStatus(
      @AuthenticationPrincipal CustomUser customUser,
      @PathVariable String chatroomId,
      @PathVariable String messageId) {
    log.info(
        "메시지 읽음 상태 조회 요청 -> 사용자: {}, 채팅방ID: {}, 메시지ID: {}",
        customUser.getUsername(),
        chatroomId,
        messageId);
    MessageReadStatusResponse response =
        messageQueryService.getMessageReadStatus(customUser.getUserId(), chatroomId, messageId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
