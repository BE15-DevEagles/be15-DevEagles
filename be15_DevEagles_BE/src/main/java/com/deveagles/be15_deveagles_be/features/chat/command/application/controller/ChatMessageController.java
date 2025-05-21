package com.deveagles.be15_deveagles_be.features.chat.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatMessageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/messages")
@Tag(name = "메시지 관리", description = "메시지 관리 API (관리자용)")
public class ChatMessageController {

  private final ChatMessageService chatMessageService;
  private final ChatMessageRepository chatMessageRepository;

  public ChatMessageController(
      ChatMessageService chatMessageService, ChatMessageRepository chatMessageRepository) {
    this.chatMessageService = chatMessageService;
    this.chatMessageRepository = chatMessageRepository;
  }

  @GetMapping("/chatroom/{chatroomId}")
  @Operation(summary = "채팅방 메시지 조회 (관리자용)", description = "채팅방의 메시지를 페이지로 조회합니다")
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
  public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessagesByChatroom(
      @AuthenticationPrincipal CustomUser customUser,
      @PathVariable String chatroomId,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "20") int size) {
    List<ChatMessageResponse> messages =
        chatMessageService.getMessagesByChatroom(chatroomId, page, size);
    return ResponseEntity.ok(ApiResponse.success(messages));
  }

  @GetMapping("/{messageId}")
  @Operation(summary = "메시지 조회 (관리자용)", description = "특정 메시지를 ID로 조회합니다")
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
  public ResponseEntity<ApiResponse<ChatMessageResponse>> getMessage(
      @AuthenticationPrincipal CustomUser customUser, @PathVariable String messageId) {
    Optional<ChatMessageResponse> message = chatMessageService.getMessage(messageId);
    return message
        .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.MESSAGE_NOT_FOUND));
  }

  @DeleteMapping("/{messageId}")
  @Operation(summary = "메시지 삭제 (관리자용)", description = "특정 메시지를 ID로 삭제합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "메시지를 찾을 수 없음",
            content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "삭제 권한 없음",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatMessageResponse>> deleteMessage(
      @PathVariable String messageId, @AuthenticationPrincipal CustomUser customUser) {
    if (customUser == null) {
      throw new ChatBusinessException(ChatErrorCode.MESSAGE_DELETE_ACCESS_DENIED);
    }

    Optional<ChatMessageResponse> deletedMessage = chatMessageService.deleteMessage(messageId);
    return deletedMessage
        .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.MESSAGE_NOT_FOUND));
  }
}
