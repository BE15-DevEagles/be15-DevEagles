package com.deveagles.be15_deveagles_be.features.chat.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.CreateChatRoomRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatRoomResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chatrooms")
public class ChatRoomController {

  private final ChatRoomService chatRoomService;

  public ChatRoomController(ChatRoomService chatRoomService) {
    this.chatRoomService = chatRoomService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(
      @RequestBody CreateChatRoomRequest request) {
    ChatRoomResponse chatRoom = chatRoomService.createChatRoom(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chatRoom));
  }

  @PostMapping("/default")
  public ResponseEntity<ApiResponse<ChatRoomResponse>> createDefaultChatRoom(
      @RequestParam String teamId, @RequestParam String name) {
    ChatRoomResponse chatRoom = chatRoomService.createDefaultChatRoom(teamId, name);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chatRoom));
  }

  @PostMapping("/ai")
  public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGetPersonalAiChatRoom(
      @RequestParam String teamId, @RequestParam String userId, @RequestParam String name) {
    ChatRoomResponse chatRoom = chatRoomService.createOrGetPersonalAiChatRoom(teamId, userId, name);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chatRoom));
  }

  @DeleteMapping("/{chatroomId}")
  public ResponseEntity<ApiResponse<ChatRoomResponse>> deleteChatRoom(
      @PathVariable String chatroomId) {
    Optional<ChatRoomResponse> deletedChatRoom = chatRoomService.deleteChatRoom(chatroomId);

    return deletedChatRoom
        .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
  }
}
