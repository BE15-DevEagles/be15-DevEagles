package com.deveagles.be15_deveagles_be.features.chat.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messages")
public class ChatMessageController {

  private final ChatMessageService chatMessageService;

  public ChatMessageController(ChatMessageService chatMessageService) {
    this.chatMessageService = chatMessageService;
  }

  @GetMapping("/chatroom/{chatroomId}")
  public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessagesByChatroom(
      @PathVariable String chatroomId,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "20") int size) {
    List<ChatMessageResponse> messages =
        chatMessageService.getMessagesByChatroom(chatroomId, page, size);
    return ResponseEntity.ok(ApiResponse.success(messages));
  }

  @GetMapping("/{messageId}")
  public ResponseEntity<ApiResponse<ChatMessageResponse>> getMessage(
      @PathVariable String messageId) {
    Optional<ChatMessageResponse> message = chatMessageService.getMessage(messageId);
    return message
        .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.MESSAGE_NOT_FOUND));
  }

  @DeleteMapping("/{messageId}")
  public ResponseEntity<ApiResponse<ChatMessageResponse>> deleteMessage(
      @PathVariable String messageId) {
    Optional<ChatMessageResponse> deletedMessage = chatMessageService.deleteMessage(messageId);
    return deletedMessage
        .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.MESSAGE_NOT_FOUND));
  }
}
