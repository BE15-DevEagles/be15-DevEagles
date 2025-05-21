package com.deveagles.be15_deveagles_be.features.chat.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request.CreateChatRoomRequest;
import com.deveagles.be15_deveagles_be.features.chat.command.application.dto.response.ChatRoomResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chatrooms")
@Tag(name = "채팅방", description = "채팅방 관리 API")
public class ChatRoomController {

  private final ChatRoomService chatRoomService;

  public ChatRoomController(ChatRoomService chatRoomService) {
    this.chatRoomService = chatRoomService;
  }

  @PostMapping
  @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "채팅방 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(
      @RequestBody CreateChatRoomRequest request, @AuthenticationPrincipal CustomUser customUser) {
    ChatRoomResponse chatRoom = chatRoomService.createChatRoom(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chatRoom));
  }

  @PostMapping("/default")
  @Operation(summary = "기본 채팅방 생성", description = "팀의 기본 채팅방을 생성합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "기본 채팅방 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> createDefaultChatRoom(
      @RequestParam String teamId,
      @RequestParam String name,
      @AuthenticationPrincipal CustomUser customUser) {
    ChatRoomResponse chatRoom = chatRoomService.createDefaultChatRoom(teamId, name);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chatRoom));
  }

  @PostMapping("/ai")
  @Operation(summary = "AI 채팅방 생성", description = "개인 AI 채팅방을 생성하거나 가져옵니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "AI 채팅방 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGetPersonalAiChatRoom(
      @RequestParam String teamId,
      @RequestParam String userId,
      @RequestParam String name,
      @AuthenticationPrincipal CustomUser customUser) {
    // 인증된 사용자 ID를 사용
    if (customUser != null) {
      userId = String.valueOf(customUser.getUserId());
    }
    ChatRoomResponse chatRoom = chatRoomService.createOrGetPersonalAiChatRoom(teamId, userId, name);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chatRoom));
  }

  @DeleteMapping("/{chatroomId}")
  @Operation(summary = "채팅방 삭제", description = "채팅방을 삭제합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "채팅방 삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "채팅방을 찾을 수 없음",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> deleteChatRoom(
      @PathVariable String chatroomId, @AuthenticationPrincipal CustomUser customUser) {
    Optional<ChatRoomResponse> deletedChatRoom = chatRoomService.deleteChatRoom(chatroomId);

    return deletedChatRoom
        .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
        .orElseThrow(() -> new ChatBusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
  }

  @PostMapping("/{chatroomId}/participants")
  @Operation(summary = "참가자 추가", description = "채팅방에 참가자를 추가합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "참가자 추가 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "채팅방을 찾을 수 없음",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> addParticipant(
      @PathVariable String chatroomId,
      @RequestParam String userId,
      @AuthenticationPrincipal CustomUser customUser) {
    ChatRoomResponse chatRoom = chatRoomService.addParticipantToChatRoom(chatroomId, userId);
    return ResponseEntity.ok(ApiResponse.success(chatRoom));
  }

  @DeleteMapping("/{chatroomId}/participants/{userId}")
  @Operation(summary = "참가자 제거", description = "채팅방에서 참가자를 제거합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "참가자 제거 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "채팅방 또는 참가자를 찾을 수 없음",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> removeParticipant(
      @PathVariable String chatroomId,
      @PathVariable String userId,
      @AuthenticationPrincipal CustomUser customUser) {
    ChatRoomResponse chatRoom = chatRoomService.removeParticipantFromChatRoom(chatroomId, userId);
    return ResponseEntity.ok(ApiResponse.success(chatRoom));
  }

  @PutMapping("/{chatroomId}/participants/{userId}/notification")
  @Operation(summary = "알림 설정 토글", description = "채팅방 참가자의 알림 설정을 토글합니다")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "알림 설정 변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "채팅방 또는 참가자를 찾을 수 없음",
            content = @Content)
      })
  public ResponseEntity<ApiResponse<ChatRoomResponse>> toggleParticipantNotification(
      @PathVariable String chatroomId,
      @PathVariable String userId,
      @AuthenticationPrincipal CustomUser customUser) {
    ChatRoomResponse chatRoom = chatRoomService.toggleParticipantNotification(chatroomId, userId);
    return ResponseEntity.ok(ApiResponse.success(chatRoom));
  }
}
