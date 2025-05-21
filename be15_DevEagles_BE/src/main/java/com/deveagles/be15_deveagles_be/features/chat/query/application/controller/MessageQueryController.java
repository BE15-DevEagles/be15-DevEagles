package com.deveagles.be15_deveagles_be.features.chat.query.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageListResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageReadStatusResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.service.MessageQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "메시지 조회", description = "채팅 메시지 조회 API")
public class MessageQueryController {

  private final MessageQueryService messageQueryService;

  @GetMapping
  @Operation(summary = "메시지 조회", description = "채팅방의 메시지 목록을 조회합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "채팅방을 찾을 수 없음",
            content = @Content)
      })
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

  @GetMapping("/{messageId}/read-status")
  @Operation(summary = "메시지 읽음 상태 조회", description = "메시지의 읽음 상태를 조회합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "메시지를 찾을 수 없음",
            content = @Content)
      })
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
