package com.deveagles.be15_deveagles_be.features.chat.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.common.dto.PagedResponse;
import com.deveagles.be15_deveagles_be.common.dto.PagedResult;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatMessageResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatMessageService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatMessageRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messages")
public class ChatMessageController {

  private final ChatMessageService chatMessageService;
  private final ChatMessageRepository chatMessageRepository;

  public ChatMessageController(
      ChatMessageService chatMessageService, ChatMessageRepository chatMessageRepository) {
    this.chatMessageService = chatMessageService;
    this.chatMessageRepository = chatMessageRepository;
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

  @GetMapping("/chatroom/{chatroomId}/messages")
  public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
      @PathVariable String chatroomId,
      @RequestParam(required = false) String before,
      @RequestParam(required = false) String after,
      @RequestParam(required = false, defaultValue = "20") int limit) {

    List<ChatMessageResponse> messages;

    if (before != null) {
      messages = chatMessageService.getMessagesByChatroomBefore(chatroomId, before, limit);
    } else if (after != null) {
      messages = chatMessageService.getMessagesByChatroomAfter(chatroomId, after, limit);
    } else {
      messages = chatMessageService.getMessagesByChatroom(chatroomId, 0, limit);
    }

    return ResponseEntity.ok(ApiResponse.success(messages));
  }

  // TODO: 관리자용 페이징 모드(필요? 삭제?)
  @GetMapping("/chatroom/{chatroomId}/messages/paged")
  public ResponseEntity<ApiResponse<PagedResponse<ChatMessageResponse>>> getMessagesWithPagination(
      @PathVariable String chatroomId,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "20") int size) {

    PagedResult<ChatMessage> pagedResult =
        chatMessageRepository.findMessagesByChatroomIdWithPagination(chatroomId, page, size);

    List<ChatMessageResponse> messages =
        pagedResult.getContent().stream()
            .map(ChatMessageResponse::from)
            .collect(Collectors.toList());

    PagedResponse<ChatMessageResponse> response =
        new PagedResponse<>(messages, pagedResult.getPagination());

    return ResponseEntity.ok(ApiResponse.success(response));
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
