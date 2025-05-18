package com.deveagles.be15_deveagles_be.features.chat.command.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.common.exception.BusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** 채팅 도메인 예외 처리 테스트 */
class ChatExceptionTest {

  @Test
  @DisplayName("채팅방을 찾을 수 없을 때 예외가 발생하는지 확인")
  void testChatRoomNotFoundException() {
    // given
    ChatErrorCode errorCode = ChatErrorCode.CHAT_ROOM_NOT_FOUND;

    // when
    BusinessException exception =
        assertThrows(
            ChatBusinessException.class,
            () -> {
              throw new ChatBusinessException(errorCode);
            });

    // then
    assertEquals(errorCode.getCode(), exception.getCode());
    assertEquals(errorCode.getMessage(), exception.getOriginalMessage());
    assertEquals(errorCode.getHttpStatus(), exception.getHttpStatus());
    assertEquals("[" + errorCode.getCode() + "] " + errorCode.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("기본 채팅방 삭제 시도 시 예외가 발생하는지 확인")
  void testDefaultChatRoomCannotBeDeleted() {
    // given
    ChatErrorCode errorCode = ChatErrorCode.DEFAULT_CHATROOM_CANNOT_BE_DELETED;

    // when
    BusinessException exception =
        assertThrows(
            ChatBusinessException.class,
            () -> {
              throw new ChatBusinessException(errorCode);
            });

    // then
    assertEquals(errorCode.getCode(), exception.getCode());
    assertEquals(errorCode.getMessage(), exception.getOriginalMessage());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
  }

  @Test
  @DisplayName("커스텀 메시지로 예외 발생 시 메시지가 올바르게 설정되는지 확인")
  void testCustomMessageException() {
    // given
    ChatErrorCode errorCode = ChatErrorCode.CHAT_ROOM_NOT_FOUND;
    String customMessage = "채팅방 ID: chatroom-123은 존재하지 않습니다.";

    // when
    BusinessException exception =
        assertThrows(
            ChatBusinessException.class,
            () -> {
              throw new ChatBusinessException(errorCode, customMessage);
            });

    // then
    assertEquals(errorCode.getCode(), exception.getCode());
    assertEquals(customMessage, exception.getOriginalMessage());
    assertEquals(errorCode.getHttpStatus(), exception.getHttpStatus());
    assertEquals("[" + errorCode.getCode() + "] " + customMessage, exception.getMessage());
  }

  @Test
  @DisplayName("웹소켓 메시지 형식 오류 예외 확인")
  void testWebSocketMessageFormatInvalid() {
    // given
    ChatErrorCode errorCode = ChatErrorCode.WEBSOCKET_MESSAGE_FORMAT_INVALID;

    // when
    BusinessException exception =
        assertThrows(
            ChatBusinessException.class,
            () -> {
              throw new ChatBusinessException(errorCode);
            });

    // then
    assertEquals(errorCode.getCode(), exception.getCode());
    assertEquals(errorCode.getMessage(), exception.getOriginalMessage());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
  }
}
